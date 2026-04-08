package com.amartha.loan.domain.service;

import com.amartha.loan.api.dto.response.LoanResponse;
import com.amartha.loan.domain.event.LoanFullyFundedEvent;
import com.amartha.loan.domain.model.*;
import com.amartha.loan.domain.repository.*;
import com.amartha.loan.domain.validator.LoanStateValidator;
import com.amartha.loan.domain.exception.LoanNotFoundException;
import com.amartha.loan.infrastructure.pdf.AgreementLetterGenerator;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class LoanService {

    @Inject
    LoanRepository loanRepository;

    @Inject
    LoanApprovalRepository loanApprovalRepository;

    @Inject
    LoanInvestmentRepository loanInvestmentRepository;

    @Inject
    LoanDisbursementRepository loanDisbursementRepository;

    @Inject
    LoanStateValidator loanStateValidator;

    @Inject
    Event<LoanFullyFundedEvent> loanFullyFundedEvent;

    @Inject
    AgreementLetterGenerator agreementLetterGenerator;

    @WithTransaction
    public Uni<Loan> createLoanReactive(Integer borrowerId, BigDecimal principalAmount, BigDecimal rate, BigDecimal roi) {
        Loan loan = new Loan();
        loan.borrowerId = borrowerId;
        loan.principalAmount = principalAmount;
        loan.rate = rate;
        loan.roi = roi;
        loan.status = LoanStatus.PROPOSED;
        return loanRepository.persist(loan).map(v -> loan);
    }

    public Loan getLoan(Long loanId) {
        Loan loan = loanRepository.findByIdSync(loanId);
        if (loan == null) {
            throw new LoanNotFoundException(loanId.toString());
        }
        return loan;
    }

    @WithTransaction
    public Uni<LoanResponse> getLoanDetailsReactive(Long loanId) {
        return loanRepository.findByIdReactive(loanId)
                .onItem().ifNull().failWith(new LoanNotFoundException(loanId.toString()))
                .flatMap(loan -> 
                    Uni.combine().all()
                        .unis(
                            loanInvestmentRepository.sumAmountByLoanIdReactive(loanId),
                            loanApprovalRepository.findByLoanIdReactive(loanId),
                            loanDisbursementRepository.findByLoanIdReactive(loanId)
                        )
                        .with((totalInvested, approval, disbursement) -> 
                            toLoanResponse(loan, totalInvested, approval.orElse(null), disbursement.orElse(null))
                        )
                );
    }

    @WithTransaction
    public Uni<List<LoanResponse>> listLoansWithDetailsReactive(LoanStatus status) {
        return listLoansReactive(status)
                .flatMap(loans -> {
                    if (loans.isEmpty()) {
                        return Uni.createFrom().item(List.<LoanResponse>of());
                    }
                    
                    List<Uni<LoanResponse>> loanDetailsUnis = loans.stream()
                            .map(loan -> 
                                // For each loan, combine its investment details, approval, and disbursement in parallel
                                Uni.combine().all()
                                    .unis(
                                        loanInvestmentRepository.sumAmountByLoanIdReactive(loan.id),
                                        loanApprovalRepository.findByLoanIdReactive(loan.id),
                                        loanDisbursementRepository.findByLoanIdReactive(loan.id)
                                    )
                                    .with((totalInvested, approval, disbursement) -> 
                                        toLoanResponse(loan, totalInvested, approval.orElse(null), disbursement.orElse(null))
                                    )
                            )
                            .toList();
                    
                    // Combine all loan response Uni objects and return as List
                    return (Uni<List<LoanResponse>>) (Uni<?>) Uni.combine().all().unis(loanDetailsUnis).with(responses -> responses);
                });
    }

    @WithTransaction
    public Uni<LoanResponse> approveLoanAndFetchDetails(Long loanId, Integer employeeId, String photoProofPath, LocalDate approvalDate) throws IOException {
        Loan loan = approveLoan(loanId, employeeId, photoProofPath, approvalDate);

        return Uni.combine().all()
                .unis(
                    loanInvestmentRepository.sumAmountByLoanIdReactive(loanId),
                    loanApprovalRepository.findByLoanIdReactive(loanId)
                )
                .with((totalInvested, approval) ->
                    toLoanResponse(loan, totalInvested, approval.orElse(null), null)
                );
    }

    @WithTransaction
    public Uni<LoanResponse> disburseLoanAndFetchDetails(Long loanId, Integer employeeId, String signedAgreementPath, LocalDate disbursementDate) {
        Loan loan = disburseLoan(loanId, employeeId, signedAgreementPath, disbursementDate);

        return Uni.combine().all()
                .unis(
                    loanInvestmentRepository.sumAmountByLoanIdReactive(loanId),
                    loanDisbursementRepository.findByLoanIdReactive(loanId)
                )
                .with((totalInvested, disbursement) ->
                    toLoanResponse(loan, totalInvested, null, disbursement.orElse(null))
                );
    }

    private LoanResponse toLoanResponse(Loan loan, BigDecimal totalInvested, LoanApproval approval, LoanDisbursement disbursement) {
        LoanResponse response = new LoanResponse();
        response.id = loan.id;
        response.borrowerId = loan.borrowerId;
        response.principalAmount = loan.principalAmount;
        response.rate = loan.rate;
        response.roi = loan.roi;
        response.status = loan.status;
        response.agreementLetterUrl = loan.agreementLetterUrl;
        response.createdAt = loan.createdAt;
        response.totalInvested = totalInvested != null ? totalInvested : BigDecimal.ZERO;
        response.remainingAmount = loan.principalAmount.subtract(response.totalInvested);

        if (approval != null) {
            response.approval = new LoanResponse.ApprovalInfo();
            response.approval.fieldValidatorEmployeeId = approval.fieldValidatorEmployeeId;
            response.approval.approvalDate = approval.approvalDate.toString();
        }

        if (disbursement != null) {
            response.disbursement = new LoanResponse.DisbursementInfo();
            response.disbursement.fieldOfficerEmployeeId = disbursement.fieldOfficerEmployeeId;
            response.disbursement.disbursementDate = disbursement.disbursementDate.toString();
        }

        return response;
    }

    public Uni<List<Loan>> listLoansReactive(LoanStatus status) {
        if (status != null) {
            return loanRepository.findByStatusReactive(status);
        }
        return loanRepository.getAllReactive();
    }

    @Transactional
    public Loan approveLoan(Long loanId, Integer employeeId, String photoProofPath, LocalDate approvalDate) throws IOException {
        Loan loan = getLoan(loanId);
        loanStateValidator.validateTransitionToApproved(loan.status);

        loan.status = LoanStatus.APPROVED;
        String agreementLetterPath = agreementLetterGenerator.generateAgreementLetter(loanId.toString(), loan.borrowerId.toString(), loan.principalAmount.toString());
        loan.agreementLetterUrl = agreementLetterPath;
        loanRepository.persist(loan);

        LoanApproval approval = new LoanApproval();
        approval.loanId = loanId;
        approval.fieldValidatorEmployeeId = employeeId;
        approval.fieldValidatorPhotoProofPath = photoProofPath;
        approval.approvalDate = approvalDate;
        loanApprovalRepository.persist(approval);

        return loan;
    }

    @Transactional
    public Loan addInvestment(Long loanId, Integer investorId, BigDecimal amount) {
        Loan loan = getLoan(loanId);

        loanStateValidator.validateTransitionToInvested(loan.status);

        BigDecimal totalInvested = loanInvestmentRepository.sumAmountByLoanId(loanId);
        loanStateValidator.validateInvestmentAmount(loan.principalAmount, totalInvested, amount);

        LoanInvestment investment = new LoanInvestment();
        investment.loanId = loanId;
        investment.investorId = investorId;
        investment.amount = amount;
        loanInvestmentRepository.persist(investment);

        BigDecimal newTotal = totalInvested.add(amount);

        if (newTotal.compareTo(loan.principalAmount) == 0) {
            loan.status = LoanStatus.INVESTED;
            loanRepository.persist(loan);

            // Fetch all investor emails from their member profiles
            List<Integer> investorIds = loanInvestmentRepository.findByLoanId(loanId).stream()
                    .map(inv -> inv.investorId)
                    .distinct()
                    .toList();

            loanFullyFundedEvent.fire(new LoanFullyFundedEvent(loanId, loan.borrowerId, investorIds));
        }

        return loan;
    }

    public List<LoanInvestment> getInvestments(Long loanId) {
        return loanInvestmentRepository.findByLoanId(loanId);
    }

    @Transactional
    public Loan disburseLoan(Long loanId, Integer employeeId, String signedAgreementPath, LocalDate disbursementDate) {
        Loan loan = getLoan(loanId);
        loanStateValidator.validateTransitionToDisbursed(loan.status);

        loan.status = LoanStatus.DISBURSED;
        loanRepository.persist(loan);

        LoanDisbursement disbursement = new LoanDisbursement();
        disbursement.loanId = loanId;
        disbursement.fieldOfficerEmployeeId = employeeId;
        disbursement.signedAgreementLetterPath = signedAgreementPath;
        disbursement.disbursementDate = disbursementDate;
        loanDisbursementRepository.persist(disbursement);

        return loan;
    }
}

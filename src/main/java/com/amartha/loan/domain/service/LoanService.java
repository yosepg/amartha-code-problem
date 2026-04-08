package com.amartha.loan.domain.service;

import com.amartha.loan.api.dto.response.LoanResponse;
import com.amartha.loan.domain.event.LoanFullyFundedEvent;
import com.amartha.loan.domain.model.*;
import com.amartha.loan.domain.repository.*;
import com.amartha.loan.domain.validator.LoanStateValidator;
import com.amartha.loan.domain.exception.LoanNotFoundException;
import com.amartha.loan.infrastructure.pdf.AgreementLetterGenerator;
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

    @Transactional
    public Loan createLoan(Integer borrowerId, BigDecimal principalAmount, BigDecimal rate, BigDecimal roi) {
        Loan loan = new Loan();
        loan.borrowerId = borrowerId;
        loan.principalAmount = principalAmount;
        loan.rate = rate;
        loan.roi = roi;
        loan.status = LoanStatus.PROPOSED;
        loanRepository.persist(loan);
        return loan;
    }

    public Loan getLoan(Long loanId) {
        Loan loan = loanRepository.findByIdSync(loanId);
        if (loan == null) {
            throw new LoanNotFoundException(loanId.toString());
        }
        return loan;
    }

    @Transactional
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

    @Transactional
    public Uni<List<LoanResponse>> listLoansWithDetailsReactive(LoanStatus status) {
        return Uni.createFrom().item(() -> {
            LoanStatus loanStatus = status;
            List<Loan> loans = loanStatus != null ? listLoans(loanStatus) : listLoans(null);
            
            return loans.stream()
                    .map(loan -> {
                        BigDecimal totalInvested = loanInvestmentRepository.sumAmountByLoanId(loan.id);
                        var approval = loanApprovalRepository.findByLoanId(loan.id);
                        var disbursement = loanDisbursementRepository.findByLoanId(loan.id);
                        return toLoanResponse(loan, totalInvested, approval.orElse(null), disbursement.orElse(null));
                    })
                    .toList();
        });
    }

    @Transactional
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

    @Transactional
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

    public List<Loan> listLoans(LoanStatus status) {
        if (status != null) {
            return loanRepository.findByStatus(status);
        }
        return loanRepository.getAll();
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

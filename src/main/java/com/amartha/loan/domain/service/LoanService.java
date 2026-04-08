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
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import io.quarkus.hibernate.reactive.panache.common.WithSession;

@ApplicationScoped
@Slf4j
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
                .chain(loan ->
                    loanInvestmentRepository.sumAmountByLoanIdReactive(loanId)
                            .chain(totalInvested ->
                                loanApprovalRepository.findByLoanIdReactive(loanId)
                                        .chain(approval ->
                                            loanDisbursementRepository.findByLoanIdReactive(loanId)
                                                    .map(disbursement ->
                                                        toLoanResponse(loan, totalInvested, approval.orElse(null), disbursement.orElse(null))
                                                    )
                                        )
                            )
                );
    }

    private List<Loan> listLoans(LoanStatus status) {
        if (status != null) {
            log.info("Listing loans with status: {}", status);
            return loanRepository.findByStatus(status);
        }
        log.info("Listing all loans");
        return loanRepository.getAll();
    }

    private Uni<List<Loan>> listLoansReactive(LoanStatus status) {
        if (status != null) {
            log.info("Listing loans with status: {}", status);
            return loanRepository.findByStatusReactive(status);
        }
        log.info("Listing all loans");
        return loanRepository.getAllReactive();
    }

    @WithTransaction
    public Uni<List<LoanResponse>> listLoansWithDetails(LoanStatus status) {
        return listLoansReactive(status)
                .chain(loans -> {
                    if (loans.isEmpty()) {
                        return Uni.createFrom().item(List.<LoanResponse>of());
                    }
                    
                    Uni<List<LoanResponse>> result = Uni.createFrom().item(new ArrayList<LoanResponse>());
                    
                    for (Loan loan : loans) {
                        result = result.chain(list ->
                            loanInvestmentRepository.sumAmountByLoanIdReactive(loan.id)
                                    .chain(totalInvested ->
                                        loanApprovalRepository.findByLoanIdReactive(loan.id)
                                                .chain(approval ->
                                                    loanDisbursementRepository.findByLoanIdReactive(loan.id)
                                                            .map(disbursement -> {
                                                                list.add(toLoanResponse(loan, totalInvested, approval.orElse(null), disbursement.orElse(null)));
                                                                return list;
                                                            })
                                                )
                                    )
                        );
                    }
                    
                    return result;
                });
    }

    @WithTransaction
    public Uni<LoanResponse> approveLoanAndFetchDetails(Long loanId, Integer employeeId, String photoProofPath, LocalDate approvalDate) {
        return loanRepository.findByIdReactive(loanId)
                .onItem().ifNull().failWith(new LoanNotFoundException(loanId.toString()))
                .chain(loan -> {
                    loanStateValidator.validateTransitionToApproved(loan.status);
                    
                    loan.status = LoanStatus.APPROVED;
                    
                    try {
                        String agreementLetterPath = agreementLetterGenerator.generateAgreementLetter(loanId.toString(), loan.borrowerId.toString(), loan.principalAmount.toString());
                        loan.agreementLetterUrl = agreementLetterPath;
                    } catch (IOException e) {
                        return Uni.createFrom().failure(e);
                    }
                    
                    return loanRepository.persist(loan)
                            .chain(v -> {
                                LoanApproval approval = new LoanApproval();
                                approval.loanId = loanId;
                                approval.fieldValidatorEmployeeId = employeeId;
                                approval.fieldValidatorPhotoProofPath = photoProofPath;
                                approval.approvalDate = approvalDate;
                                
                                return loanApprovalRepository.persist(approval)
                                        .chain(v2 ->
                                            loanInvestmentRepository.sumAmountByLoanIdReactive(loanId)
                                                    .chain(totalInvested ->
                                                        loanApprovalRepository.findByLoanIdReactive(loanId)
                                                                .map(approvalOpt ->
                                                                    toLoanResponse(loan, totalInvested, approvalOpt.orElse(null), null)
                                                                )
                                                    )
                                        );
                            });
                });
    }

    @WithTransaction
    public Uni<LoanResponse> disburseLoanAndFetchDetails(Long loanId, Integer employeeId, String signedAgreementPath, LocalDate disbursementDate) {
        return loanRepository.findByIdReactive(loanId)
                .onItem().ifNull().failWith(new LoanNotFoundException(loanId.toString()))
                .chain(loan -> {
                    loanStateValidator.validateTransitionToDisbursed(loan.status);
                    
                    loan.status = LoanStatus.DISBURSED;
                    
                    return loanRepository.persist(loan)
                            .chain(v -> {
                                LoanDisbursement disbursement = new LoanDisbursement();
                                disbursement.loanId = loanId;
                                disbursement.fieldOfficerEmployeeId = employeeId;
                                disbursement.signedAgreementLetterPath = signedAgreementPath;
                                disbursement.disbursementDate = disbursementDate;
                                
                                return loanDisbursementRepository.persist(disbursement)
                                        .chain(v2 ->
                                            loanInvestmentRepository.sumAmountByLoanIdReactive(loanId)
                                                    .chain(totalInvested ->
                                                        loanApprovalRepository.findByLoanIdReactive(loanId)
                                                                .chain(approval ->
                                                                    loanDisbursementRepository.findByLoanIdReactive(loanId)
                                                                            .map(disbursementOpt ->
                                                                                toLoanResponse(loan, totalInvested, approval.orElse(null), disbursementOpt.orElse(null))
                                                                            )
                                                                )
                                                    )
                                        );
                            });
                });
    }

    private LoanResponse toLoanResponse(Loan loan, BigDecimal totalInvested, LoanApproval approval, LoanDisbursement disbursement) {
        BigDecimal invested = totalInvested != null ? totalInvested : BigDecimal.ZERO;
        
        LoanResponse.ApprovalInfo approvalInfo = null;
        if (approval != null) {
            approvalInfo = new LoanResponse.ApprovalInfo(
                approval.fieldValidatorEmployeeId,
                approval.approvalDate.toString()
            );
        }

        LoanResponse.DisbursementInfo disbursementInfo = null;
        if (disbursement != null) {
            disbursementInfo = new LoanResponse.DisbursementInfo(
                disbursement.fieldOfficerEmployeeId,
                disbursement.disbursementDate.toString()
            );
        }

        return new LoanResponse(
            loan.id,
            loan.borrowerId,
            loan.principalAmount,
            loan.rate,
            loan.roi,
            loan.status,
            invested,
            loan.principalAmount.subtract(invested),
            loan.agreementLetterUrl,
            approvalInfo,
            disbursementInfo,
            loan.createdAt
        );
    }


    
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
    // deprecated
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

    @WithTransaction
    public Uni<LoanInvestment> addInvestmentReactive(Long loanId, Integer investorId, BigDecimal amount) {
        return loanRepository.findByIdReactive(loanId)
                .onItem().ifNull().failWith(new LoanNotFoundException(loanId.toString()))
                .chain(loan -> {
                    loanStateValidator.validateTransitionToInvested(loan.status);
                    
                    return loanInvestmentRepository.sumAmountByLoanIdReactive(loanId)
                            .chain(totalInvested -> {
                                loanStateValidator.validateInvestmentAmount(loan.principalAmount, totalInvested, amount);
                                
                                LoanInvestment investment = new LoanInvestment();
                                investment.loanId = loanId;
                                investment.investorId = investorId;
                                investment.amount = amount;
                                
                                return loanInvestmentRepository.persist(investment)
                                        .chain(v -> {
                                            BigDecimal newTotal = totalInvested.add(amount);
                                            
                                            if (newTotal.compareTo(loan.principalAmount) == 0) {
                                                loan.status = LoanStatus.INVESTED;
                                                
                                                return loanRepository.persist(loan)
                                                        .chain(v2 ->
                                                            loanInvestmentRepository.findByLoanIdReactive(loanId)
                                                                    .map(investments -> {
                                                                        List<Integer> investorIds = investments.stream()
                                                                                .map(inv -> inv.investorId)
                                                                                .distinct()
                                                                                .toList();
                                                                        
                                                                        loanFullyFundedEvent.fire(new LoanFullyFundedEvent(loanId, loan.borrowerId, investorIds));
                                                                        return investment;
                                                                    })
                                                        );
                                            }
                                            
                                            return Uni.createFrom().item(investment);
                                        });
                            });
                });
    }

    public List<LoanInvestment> getInvestments(Long loanId) {
        return loanInvestmentRepository.findByLoanId(loanId);
    }

    @WithTransaction
    public Uni<List<LoanInvestment>> getInvestmentsReactive(Long loanId) {
        return loanInvestmentRepository.findByLoanIdReactive(loanId);
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

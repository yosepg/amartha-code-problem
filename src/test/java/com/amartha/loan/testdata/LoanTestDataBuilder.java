package com.amartha.loan.testdata;

import com.amartha.loan.api.dto.request.AddInvestmentRequest;
import com.amartha.loan.api.dto.request.ApproveRequest;
import com.amartha.loan.api.dto.request.CreateLoanRequest;
import com.amartha.loan.api.dto.request.DisburseRequest;
import com.amartha.loan.domain.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class LoanTestDataBuilder {

    // Request DTOs
    public static CreateLoanRequest validCreateLoanRequest() {
        return new CreateLoanRequest(
                1,
                new BigDecimal("5000000.00"),
                new BigDecimal("5.5"),
                new BigDecimal("12.0")
        );
    }

    public static AddInvestmentRequest validAddInvestmentRequest(Long loanId) {
        return new AddInvestmentRequest(
                loanId,
                101,
                new BigDecimal("1000000.00"),
                "investor@example.com"
        );
    }

    public static ApproveRequest validApproveRequest() {
        return new ApproveRequest(
                1,
                "/path/to/photo.jpg",
                LocalDate.now()
        );
    }

    public static DisburseRequest validDisburseRequest() {
        return new DisburseRequest(
                2,
                "/path/to/signed-agreement.pdf",
                LocalDate.now()
        );
    }

    // Domain Entities
    public static Loan proposedLoan() {
        Loan loan = new Loan();
        loan.id = 1L;
        loan.borrowerId = 1;
        loan.principalAmount = new BigDecimal("5000000.00");
        loan.rate = new BigDecimal("5.5");
        loan.roi = new BigDecimal("12.0");
        loan.status = LoanStatus.PROPOSED;
        loan.createdAt = LocalDateTime.now();
        return loan;
    }

    public static Loan approvedLoan() {
        Loan loan = proposedLoan();
        loan.id = 2L;
        loan.status = LoanStatus.APPROVED;
        loan.agreementLetterUrl = "/path/to/agreement.pdf";
        return loan;
    }

    public static Loan investedLoan() {
        Loan loan = approvedLoan();
        loan.id = 3L;
        loan.status = LoanStatus.INVESTED;
        return loan;
    }

    public static Loan disbursedLoan() {
        Loan loan = investedLoan();
        loan.id = 4L;
        loan.status = LoanStatus.DISBURSED;
        return loan;
    }

    public static LoanInvestment investment(Long loanId, BigDecimal amount) {
        LoanInvestment investment = new LoanInvestment();
        investment.id = 100L;
        investment.loanId = loanId;
        investment.investorId = 101;
        investment.amount = amount;
        investment.investedAt = LocalDateTime.now();
        return investment;
    }

    public static LoanApproval approval(Long loanId) {
        LoanApproval approval = new LoanApproval();
        approval.loanId = loanId;
        approval.fieldValidatorEmployeeId = 1;
        approval.fieldValidatorPhotoProofPath = "/path/to/photo.jpg";
        approval.approvalDate = LocalDate.now();
        return approval;
    }

    public static LoanDisbursement disbursement(Long loanId) {
        LoanDisbursement disbursement = new LoanDisbursement();
        disbursement.loanId = loanId;
        disbursement.fieldOfficerEmployeeId = 2;
        disbursement.signedAgreementLetterPath = "/path/to/signed.pdf";
        disbursement.disbursementDate = LocalDate.now();
        return disbursement;
    }
}

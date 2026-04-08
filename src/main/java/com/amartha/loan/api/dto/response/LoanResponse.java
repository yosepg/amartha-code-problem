package com.amartha.loan.api.dto.response;

import com.amartha.loan.domain.model.LoanStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LoanResponse {

    public Long id;
    public Integer borrowerId;
    public BigDecimal principalAmount;
    public BigDecimal rate;
    public BigDecimal roi;
    public LoanStatus status;
    public BigDecimal totalInvested;
    public BigDecimal remainingAmount;
    public String agreementLetterUrl;
    public ApprovalInfo approval;
    public DisbursementInfo disbursement;
    public LocalDateTime createdAt;

    public static class ApprovalInfo {
        public Integer fieldValidatorEmployeeId;
        public String approvalDate;
    }

    public static class DisbursementInfo {
        public Integer fieldOfficerEmployeeId;
        public String disbursementDate;
    }

    public LoanResponse() {
    }
}

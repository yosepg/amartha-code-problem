package com.amartha.loan.api.dto.response;

import com.amartha.loan.domain.model.LoanStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LoanResponse(
    Long id,
    Integer borrowerId,
    BigDecimal principalAmount,
    BigDecimal rate,
    BigDecimal roi,
    LoanStatus status,
    BigDecimal totalInvested,
    BigDecimal remainingAmount,
    String agreementLetterUrl,
    ApprovalInfo approval,
    DisbursementInfo disbursement,
    LocalDateTime createdAt
) {
    public record ApprovalInfo(
        Integer fieldValidatorEmployeeId,
        String approvalDate
    ) {}

    public record DisbursementInfo(
        Integer fieldOfficerEmployeeId,
        String disbursementDate
    ) {}
}

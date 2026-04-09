package com.amartha.loan.api.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record DisburseRequest(
    @NotNull(message = "Employee ID is required")
    Integer employeeId,

    @NotNull(message = "Signed agreement letter path is required")
    String signedAgreementLetterPath,

    @NotNull(message = "Disbursement date is required")
    LocalDate disbursementDate
) {}

package com.amartha.loan.api.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ApproveRequest(
    @NotNull(message = "Employee ID is required")
    Integer employeeId,

    @NotNull(message = "Photo proof path is required")
    String photoProofPath,

    @NotNull(message = "Approval date is required")
    LocalDate approvalDate
) {}

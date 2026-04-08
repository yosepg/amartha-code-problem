package com.amartha.loan.api.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class ApproveRequest {

    @NotNull(message = "Employee ID is required")
    public Integer employeeId;

    @NotNull(message = "Photo proof path is required")
    public String photoProofPath;

    @NotNull(message = "Approval date is required")
    public LocalDate approvalDate;

    public ApproveRequest() {
    }

    public ApproveRequest(Integer employeeId, String photoProofPath, LocalDate approvalDate) {
        this.employeeId = employeeId;
        this.photoProofPath = photoProofPath;
        this.approvalDate = approvalDate;
    }
}

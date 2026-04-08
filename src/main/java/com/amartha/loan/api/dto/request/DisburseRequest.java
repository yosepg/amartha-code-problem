package com.amartha.loan.api.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class DisburseRequest {

    @NotNull(message = "Employee ID is required")
    public Integer employeeId;

    @NotNull(message = "Signed agreement letter path is required")
    public String signedAgreementLetterPath;

    @NotNull(message = "Disbursement date is required")
    public LocalDate disbursementDate;

    public DisburseRequest() {
    }

    public DisburseRequest(Integer employeeId, String signedAgreementLetterPath, LocalDate disbursementDate) {
        this.employeeId = employeeId;
        this.signedAgreementLetterPath = signedAgreementLetterPath;
        this.disbursementDate = disbursementDate;
    }
}

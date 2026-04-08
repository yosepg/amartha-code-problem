package com.amartha.loan.api.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class CreateLoanRequest {

    @NotNull(message = "Borrower ID is required")
    public Integer borrowerId;

    @NotNull(message = "Principal amount is required")
    @DecimalMin(value = "10", message = "Principal amount must be positive")
    public BigDecimal principalAmount;

    @NotNull(message = "Rate is required")
    @DecimalMin(value = "1", message = "Rate must be >= 0")
    public BigDecimal rate;

    @NotNull(message = "ROI is required")
    @DecimalMin(value = "1", message = "ROI must be >= 0")
    public BigDecimal roi;

    public CreateLoanRequest() {
    }

    public CreateLoanRequest(Integer borrowerId, BigDecimal principalAmount, BigDecimal rate, BigDecimal roi) {
        this.borrowerId = borrowerId;
        this.principalAmount = principalAmount;
        this.rate = rate;
        this.roi = roi;
    }
}

package com.amartha.loan.api.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateLoanRequest(
    @NotNull(message = "Borrower ID is required")
    Integer borrowerId,

    @NotNull(message = "Principal amount is required")
    @DecimalMin(value = "10", message = "Principal amount must be positive")
    BigDecimal principalAmount,

    @NotNull(message = "Rate is required")
    @DecimalMin(value = "1", message = "Rate must be >= 0")
    BigDecimal rate,

    @NotNull(message = "ROI is required")
    @DecimalMin(value = "1", message = "ROI must be >= 0")
    BigDecimal roi
) {}

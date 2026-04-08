package com.amartha.loan.api.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record AddInvestmentRequest(
    @NotNull(message = "Loan ID is required")
    Long loanId,

    @NotNull(message = "Investor ID is required")
    Integer investorId,

    @NotNull(message = "Investment amount is required")
    @DecimalMin(value = "10", message = "Investment amount must be positive")
    BigDecimal amount,

    @NotNull(message = "Investor email is required")
    String investorEmail
) {}

package com.amartha.loan.api.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class AddInvestmentRequest {

    @NotNull(message = "Investor ID is required")
    public Integer investorId;

    @NotNull(message = "Investment amount is required")
    @DecimalMin(value = "0.01", message = "Investment amount must be positive")
    public BigDecimal amount;

    public AddInvestmentRequest() {
    }

    public AddInvestmentRequest(Integer investorId, BigDecimal amount) {
        this.investorId = investorId;
        this.amount = amount;
    }
}

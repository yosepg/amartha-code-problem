package com.amartha.loan.api.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class AddInvestmentRequest {

    @NotNull(message = "Loan ID is required")
    public Long loanId;

    @NotNull(message = "Investor ID is required")
    public Integer investorId;

    @NotNull(message = "Investment amount is required")
    @DecimalMin(value = "10", message = "Investment amount must be positive")
    public BigDecimal amount;

    @NotNull(message = "Investor email is required")
    public String investorEmail;

    public AddInvestmentRequest() {
    }

    public AddInvestmentRequest(Long loanId, Integer investorId, BigDecimal amount) {
        this.loanId = loanId;
        this.investorId = investorId;
        this.amount = amount;
    }
}

package com.amartha.loan.domain.exception;

import java.math.BigDecimal;

public class InvestmentExceedsPrincipalException extends RuntimeException {
    private final BigDecimal principalAmount;
    private final BigDecimal totalInvested;
    private final BigDecimal attemptedInvestment;

    public InvestmentExceedsPrincipalException(
            BigDecimal principalAmount,
            BigDecimal totalInvested,
            BigDecimal attemptedInvestment) {
        super(String.format(
                "Investment of %s would exceed principal. Principal: %s, Total Invested: %s",
                attemptedInvestment, principalAmount, totalInvested));
        this.principalAmount = principalAmount;
        this.totalInvested = totalInvested;
        this.attemptedInvestment = attemptedInvestment;
    }

    public BigDecimal getPrincipalAmount() {
        return principalAmount;
    }

    public BigDecimal getTotalInvested() {
        return totalInvested;
    }

    public BigDecimal getAttemptedInvestment() {
        return attemptedInvestment;
    }
}

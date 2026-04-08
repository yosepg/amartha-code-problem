package com.amartha.loan.domain.validator;

import com.amartha.loan.domain.exception.InvalidLoanStateException;
import com.amartha.loan.domain.exception.InvestmentExceedsPrincipalException;
import com.amartha.loan.domain.model.LoanStatus;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;

@ApplicationScoped
public class LoanStateValidator {

    public void validateTransitionToApproved(LoanStatus currentStatus) {
        if (currentStatus != LoanStatus.PROPOSED) {
            throw new InvalidLoanStateException(
                    currentStatus.name(),
                    "Cannot approve loan that is not in PROPOSED state");
        }
    }

    public void validateTransitionToInvested(LoanStatus currentStatus) {
        if (currentStatus != LoanStatus.APPROVED) {
            throw new InvalidLoanStateException(
                    currentStatus.name(),
                    "Cannot invest in loan that is not APPROVED");
        }
    }

    public void validateTransitionToDisbursed(LoanStatus currentStatus) {
        if (currentStatus != LoanStatus.INVESTED) {
            throw new InvalidLoanStateException(
                    currentStatus.name(),
                    "Cannot disburse loan that is not INVESTED");
        }
    }

    public void validateInvestmentAmount(BigDecimal principalAmount, BigDecimal totalInvested, BigDecimal newInvestmentAmount) {
        if (newInvestmentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Investment amount must be positive");
        }
        BigDecimal remaining = principalAmount.subtract(totalInvested);
        if (newInvestmentAmount.compareTo(remaining) > 0) {
            throw new InvestmentExceedsPrincipalException(principalAmount, totalInvested, newInvestmentAmount);
        }
    }
}

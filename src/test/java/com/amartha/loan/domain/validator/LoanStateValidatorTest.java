package com.amartha.loan.domain.validator;

import com.amartha.loan.domain.exception.InvalidLoanStateException;
import com.amartha.loan.domain.exception.InvestmentExceedsPrincipalException;
import com.amartha.loan.domain.model.LoanStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class LoanStateValidatorTest {

    private LoanStateValidator validator;

    @BeforeEach
    void setUp() {
        validator = new LoanStateValidator();
    }

    @Test
    void validateTransitionToApproved_fromProposed_succeeds() {
        assertDoesNotThrow(() -> validator.validateTransitionToApproved(LoanStatus.PROPOSED));
    }

    @Test
    void validateTransitionToApproved_fromNonProposed_throwsException() {
        for (LoanStatus status : new LoanStatus[]{LoanStatus.APPROVED, LoanStatus.INVESTED, LoanStatus.DISBURSED}) {
            assertThrows(InvalidLoanStateException.class, () -> validator.validateTransitionToApproved(status));
        }
    }

    @Test
    void validateTransitionToInvested_fromApproved_succeeds() {
        assertDoesNotThrow(() -> validator.validateTransitionToInvested(LoanStatus.APPROVED));
    }

    @Test
    void validateTransitionToInvested_fromNonApproved_throwsException() {
        for (LoanStatus status : new LoanStatus[]{LoanStatus.PROPOSED, LoanStatus.INVESTED, LoanStatus.DISBURSED}) {
            assertThrows(InvalidLoanStateException.class, () -> validator.validateTransitionToInvested(status));
        }
    }

    @Test
    void validateTransitionToDisbursed_fromInvested_succeeds() {
        assertDoesNotThrow(() -> validator.validateTransitionToDisbursed(LoanStatus.INVESTED));
    }

    @Test
    void validateTransitionToDisbursed_fromNonInvested_throwsException() {
        for (LoanStatus status : new LoanStatus[]{LoanStatus.PROPOSED, LoanStatus.APPROVED, LoanStatus.DISBURSED}) {
            assertThrows(InvalidLoanStateException.class, () -> validator.validateTransitionToDisbursed(status));
        }
    }

    @Test
    void validateInvestmentAmount_withinBounds_succeeds() {
        BigDecimal principal = new BigDecimal("5000000.00");
        BigDecimal invested = new BigDecimal("2000000.00");
        BigDecimal newInvestment = new BigDecimal("3000000.00");

        assertDoesNotThrow(() -> validator.validateInvestmentAmount(principal, invested, newInvestment));
    }

    @Test
    void validateInvestmentAmount_exceedsBounds_throwsException() {
        BigDecimal principal = new BigDecimal("5000000.00");
        BigDecimal invested = new BigDecimal("2000000.00");
        BigDecimal newInvestment = new BigDecimal("3000001.00");

        assertThrows(InvestmentExceedsPrincipalException.class, 
                () -> validator.validateInvestmentAmount(principal, invested, newInvestment));
    }
}

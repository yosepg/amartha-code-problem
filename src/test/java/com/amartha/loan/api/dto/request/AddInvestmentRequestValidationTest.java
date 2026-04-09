package com.amartha.loan.api.dto.request;

import com.amartha.loan.api.dto.BaseValidationTest;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AddInvestmentRequestValidationTest extends BaseValidationTest {

    @Test
    void validRequest_allFieldsProvided_passes() {
        AddInvestmentRequest request = new AddInvestmentRequest(
                1L,
                101,
                new BigDecimal("1000000.00"),
                "investor@example.com"
        );

        Set<ConstraintViolation<AddInvestmentRequest>> violations = validate(request);

        assertTrue(violations.isEmpty(), "Valid request should have no violations");
    }

    @Test
    void loanId_null_fails() {
        AddInvestmentRequest request = new AddInvestmentRequest(
                null,
                101,
                new BigDecimal("1000000.00"),
                "investor@example.com"
        );

        Set<ConstraintViolation<AddInvestmentRequest>> violations = validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(hasViolation(violations, "loanId"));
        assertEquals("Loan ID is required", getViolationMessage(violations, "loanId"));
    }

    @Test
    void investorId_null_fails() {
        AddInvestmentRequest request = new AddInvestmentRequest(
                1L,
                null,
                new BigDecimal("1000000.00"),
                "investor@example.com"
        );

        Set<ConstraintViolation<AddInvestmentRequest>> violations = validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(hasViolation(violations, "investorId"));
    }

    @Test
    void amount_null_fails() {
        AddInvestmentRequest request = new AddInvestmentRequest(
                1L,
                101,
                null,
                "investor@example.com"
        );

        Set<ConstraintViolation<AddInvestmentRequest>> violations = validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(hasViolation(violations, "amount"));
    }

    @Test
    void investorEmail_null_fails() {
        AddInvestmentRequest request = new AddInvestmentRequest(
                1L,
                101,
                new BigDecimal("1000000.00"),
                null
        );

        Set<ConstraintViolation<AddInvestmentRequest>> violations = validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(hasViolation(violations, "investorEmail"));
    }

    @Test
    void amount_lessThan10_fails() {
        AddInvestmentRequest request = new AddInvestmentRequest(
                1L,
                101,
                new BigDecimal("9.99"),
                "investor@example.com"
        );

        Set<ConstraintViolation<AddInvestmentRequest>> violations = validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(hasViolation(violations, "amount"));
    }

    @Test
    void amount_exactly10_passes() {
        AddInvestmentRequest request = new AddInvestmentRequest(
                1L,
                101,
                new BigDecimal("10.00"),
                "investor@example.com"
        );

        Set<ConstraintViolation<AddInvestmentRequest>> violations = validate(request);

        assertFalse(hasViolation(violations, "amount"));
    }
}

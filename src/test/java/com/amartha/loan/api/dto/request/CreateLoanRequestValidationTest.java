package com.amartha.loan.api.dto.request;

import com.amartha.loan.api.dto.BaseValidationTest;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CreateLoanRequestValidationTest extends BaseValidationTest {

    @Test
    void validRequest_allFieldsProvided_passes() {
        CreateLoanRequest request = new CreateLoanRequest(
                1,
                new BigDecimal("5000000.00"),
                new BigDecimal("5.5"),
                new BigDecimal("12.0")
        );

        Set<ConstraintViolation<CreateLoanRequest>> violations = validate(request);

        assertTrue(violations.isEmpty(), "Valid request should have no violations");
    }

    @Test
    void borrowerId_null_fails() {
        CreateLoanRequest request = new CreateLoanRequest(
                null,
                new BigDecimal("5000000.00"),
                new BigDecimal("5.5"),
                new BigDecimal("12.0")
        );

        Set<ConstraintViolation<CreateLoanRequest>> violations = validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(hasViolation(violations, "borrowerId"));
        assertEquals("Borrower ID is required", getViolationMessage(violations, "borrowerId"));
    }

    @Test
    void principalAmount_null_fails() {
        CreateLoanRequest request = new CreateLoanRequest(
                1,
                null,
                new BigDecimal("5.5"),
                new BigDecimal("12.0")
        );

        Set<ConstraintViolation<CreateLoanRequest>> violations = validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(hasViolation(violations, "principalAmount"));
    }

    @Test
    void rate_null_fails() {
        CreateLoanRequest request = new CreateLoanRequest(
                1,
                new BigDecimal("5000000.00"),
                null,
                new BigDecimal("12.0")
        );

        Set<ConstraintViolation<CreateLoanRequest>> violations = validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(hasViolation(violations, "rate"));
    }

    @Test
    void roi_null_fails() {
        CreateLoanRequest request = new CreateLoanRequest(
                1,
                new BigDecimal("5000000.00"),
                new BigDecimal("5.5"),
                null
        );

        Set<ConstraintViolation<CreateLoanRequest>> violations = validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(hasViolation(violations, "roi"));
    }

    @Test
    void principalAmount_lessThan10_fails() {
        CreateLoanRequest request = new CreateLoanRequest(
                1,
                new BigDecimal("9.99"),
                new BigDecimal("5.5"),
                new BigDecimal("12.0")
        );

        Set<ConstraintViolation<CreateLoanRequest>> violations = validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(hasViolation(violations, "principalAmount"));
    }

    @Test
    void principalAmount_exactly10_passes() {
        CreateLoanRequest request = new CreateLoanRequest(
                1,
                new BigDecimal("10.00"),
                new BigDecimal("5.5"),
                new BigDecimal("12.0")
        );

        Set<ConstraintViolation<CreateLoanRequest>> violations = validate(request);

        assertFalse(hasViolation(violations, "principalAmount"));
    }

    @Test
    void rate_lessThan1_fails() {
        CreateLoanRequest request = new CreateLoanRequest(
                1,
                new BigDecimal("5000000.00"),
                new BigDecimal("0.99"),
                new BigDecimal("12.0")
        );

        Set<ConstraintViolation<CreateLoanRequest>> violations = validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(hasViolation(violations, "rate"));
    }

    @Test
    void rate_exactly1_passes() {
        CreateLoanRequest request = new CreateLoanRequest(
                1,
                new BigDecimal("5000000.00"),
                new BigDecimal("1.00"),
                new BigDecimal("12.0")
        );

        Set<ConstraintViolation<CreateLoanRequest>> violations = validate(request);

        assertFalse(hasViolation(violations, "rate"));
    }

    @Test
    void roi_lessThan1_fails() {
        CreateLoanRequest request = new CreateLoanRequest(
                1,
                new BigDecimal("5000000.00"),
                new BigDecimal("5.5"),
                new BigDecimal("0.99")
        );

        Set<ConstraintViolation<CreateLoanRequest>> violations = validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(hasViolation(violations, "roi"));
    }

    @Test
    void roi_exactly1_passes() {
        CreateLoanRequest request = new CreateLoanRequest(
                1,
                new BigDecimal("5000000.00"),
                new BigDecimal("5.5"),
                new BigDecimal("1.00")
        );

        Set<ConstraintViolation<CreateLoanRequest>> violations = validate(request);

        assertFalse(hasViolation(violations, "roi"));
    }
}

package com.amartha.loan.api.dto.request;

import com.amartha.loan.api.dto.BaseValidationTest;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DisburseRequestValidationTest extends BaseValidationTest {

    @Test
    void validRequest_allFieldsProvided_passes() {
        DisburseRequest request = new DisburseRequest(
                2,
                "/path/to/signed-agreement.pdf",
                LocalDate.now()
        );

        Set<ConstraintViolation<DisburseRequest>> violations = validate(request);

        assertTrue(violations.isEmpty(), "Valid request should have no violations");
    }

    @Test
    void employeeId_null_fails() {
        DisburseRequest request = new DisburseRequest(
                null,
                "/path/to/signed-agreement.pdf",
                LocalDate.now()
        );

        Set<ConstraintViolation<DisburseRequest>> violations = validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(hasViolation(violations, "employeeId"));
        assertEquals("Employee ID is required", getViolationMessage(violations, "employeeId"));
    }

    @Test
    void signedAgreementLetterPath_null_fails() {
        DisburseRequest request = new DisburseRequest(
                2,
                null,
                LocalDate.now()
        );

        Set<ConstraintViolation<DisburseRequest>> violations = validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(hasViolation(violations, "signedAgreementLetterPath"));
    }

    @Test
    void disbursementDate_null_fails() {
        DisburseRequest request = new DisburseRequest(
                2,
                "/path/to/signed-agreement.pdf",
                null
        );

        Set<ConstraintViolation<DisburseRequest>> violations = validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(hasViolation(violations, "disbursementDate"));
    }

    @Test
    void disbursementDate_pastDate_passes() {
        DisburseRequest request = new DisburseRequest(
                2,
                "/path/to/signed-agreement.pdf",
                LocalDate.now().minusDays(1)
        );

        Set<ConstraintViolation<DisburseRequest>> violations = validate(request);

        assertFalse(hasViolation(violations, "disbursementDate"));
    }
}

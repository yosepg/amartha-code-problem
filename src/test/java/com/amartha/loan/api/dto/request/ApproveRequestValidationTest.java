package com.amartha.loan.api.dto.request;

import com.amartha.loan.api.dto.BaseValidationTest;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ApproveRequestValidationTest extends BaseValidationTest {

    @Test
    void validRequest_allFieldsProvided_passes() {
        ApproveRequest request = new ApproveRequest(
                1,
                "/path/to/photo.jpg",
                LocalDate.now()
        );

        Set<ConstraintViolation<ApproveRequest>> violations = validate(request);

        assertTrue(violations.isEmpty(), "Valid request should have no violations");
    }

    @Test
    void employeeId_null_fails() {
        ApproveRequest request = new ApproveRequest(
                null,
                "/path/to/photo.jpg",
                LocalDate.now()
        );

        Set<ConstraintViolation<ApproveRequest>> violations = validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(hasViolation(violations, "employeeId"));
        assertEquals("Employee ID is required", getViolationMessage(violations, "employeeId"));
    }

    @Test
    void photoProofPath_null_fails() {
        ApproveRequest request = new ApproveRequest(
                1,
                null,
                LocalDate.now()
        );

        Set<ConstraintViolation<ApproveRequest>> violations = validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(hasViolation(violations, "photoProofPath"));
    }

    @Test
    void approvalDate_null_fails() {
        ApproveRequest request = new ApproveRequest(
                1,
                "/path/to/photo.jpg",
                null
        );

        Set<ConstraintViolation<ApproveRequest>> violations = validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(hasViolation(violations, "approvalDate"));
    }

    @Test
    void approvalDate_pastDate_passes() {
        ApproveRequest request = new ApproveRequest(
                1,
                "/path/to/photo.jpg",
                LocalDate.now().minusDays(1)
        );

        Set<ConstraintViolation<ApproveRequest>> violations = validate(request);

        assertFalse(hasViolation(violations, "approvalDate"));
    }
}

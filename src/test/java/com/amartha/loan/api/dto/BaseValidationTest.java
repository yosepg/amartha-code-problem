package com.amartha.loan.api.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;

import java.util.Set;

public abstract class BaseValidationTest {

    protected Validator validator;

    @BeforeEach
    void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    protected <T> Set<ConstraintViolation<T>> validate(T object) {
        return validator.validate(object);
    }

    protected <T> boolean hasViolation(Set<ConstraintViolation<T>> violations, String propertyPath) {
        return violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals(propertyPath));
    }

    protected <T> String getViolationMessage(Set<ConstraintViolation<T>> violations, String propertyPath) {
        return violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals(propertyPath))
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse(null);
    }
}

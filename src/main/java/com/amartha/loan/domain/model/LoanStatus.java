package com.amartha.loan.domain.model;

public enum LoanStatus {
    PROPOSED("Loan proposed by staff"),
    APPROVED("Loan approved by field validator"),
    INVESTED("Loan fully funded by investors"),
    DISBURSED("Loan disbursed to borrower");

    private final String description;

    LoanStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

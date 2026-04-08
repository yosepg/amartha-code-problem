package com.amartha.loan.domain.exception;

import jakarta.ws.rs.NotFoundException;

public class LoanNotFoundException extends NotFoundException {

    public LoanNotFoundException(String loanId) {
        super("Loan not found: " + loanId);
    }

    public LoanNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

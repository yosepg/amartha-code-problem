package com.amartha.loan.domain.exception;

public class InvalidLoanStateException extends RuntimeException {
    private final String currentState;
    private final String attemptedTransition;

    public InvalidLoanStateException(String currentState, String attemptedTransition) {
        super(String.format("Cannot transition from %s to %s", currentState, attemptedTransition));
        this.currentState = currentState;
        this.attemptedTransition = attemptedTransition;
    }

    public InvalidLoanStateException(String message) {
        super(message);
        this.currentState = null;
        this.attemptedTransition = null;
    }

    public String getCurrentState() {
        return currentState;
    }

    public String getAttemptedTransition() {
        return attemptedTransition;
    }
}

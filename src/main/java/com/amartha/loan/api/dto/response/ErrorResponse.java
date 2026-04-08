package com.amartha.loan.api.dto.response;

public class ErrorResponse {

    public String code;
    public String message;

    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public ErrorResponse() {
    }
}

package com.amartha.loan.api.dto.response;

public record ErrorResponse(
    String code,
    String message
) {}

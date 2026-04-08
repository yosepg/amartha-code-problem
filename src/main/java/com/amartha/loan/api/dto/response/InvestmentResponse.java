package com.amartha.loan.api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InvestmentResponse(
    Long id,
    Long loanId,
    Integer investorId,
    BigDecimal amount,
    LocalDateTime investedAt
) {}

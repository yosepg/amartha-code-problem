package com.amartha.loan.api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InvestmentResponse {

    public Long id;
    public Long loanId;
    public Integer investorId;
    public BigDecimal amount;
    public LocalDateTime investedAt;

    public InvestmentResponse() {
    }
}

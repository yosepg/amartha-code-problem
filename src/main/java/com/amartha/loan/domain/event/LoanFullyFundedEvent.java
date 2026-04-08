package com.amartha.loan.domain.event;

import java.util.List;

public class LoanFullyFundedEvent {
    private final Long loanId;
    private final Integer borrowerId;
    private final List<Integer> investorIds;

    public LoanFullyFundedEvent(Long loanId, Integer borrowerId, List<Integer> investorIds) {
        this.loanId = loanId;
        this.borrowerId = borrowerId;
        this.investorIds = investorIds;
    }

    public Long getLoanId() {
        return loanId;
    }

    public Integer getBorrowerId() {
        return borrowerId;
    }

    public List<Integer> getInvestorIds() {
        return investorIds;
    }
}

package com.amartha.loan.domain.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_investments")
public class LoanInvestment extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "loan_id", nullable = false)
    public Long loanId;

    @Column(name = "investor_id", nullable = false)
    public Integer investorId;

    @Column(name = "amount", nullable = false, columnDefinition = "DECIMAL(19, 2)")
    public BigDecimal amount;

    @Column(name = "invested_at", nullable = false, updatable = false)
    public LocalDateTime investedAt;

    @PrePersist
    protected void onCreate() {
        investedAt = LocalDateTime.now();
    }
}

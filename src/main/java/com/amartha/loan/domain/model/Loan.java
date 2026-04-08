package com.amartha.loan.domain.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loans")
public class Loan extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "borrower_id", nullable = false)
    public Integer borrowerId;

    @Column(name = "principal_amount", nullable = false, columnDefinition = "DECIMAL(19, 2)")
    public BigDecimal principalAmount;

    @Column(name = "rate", nullable = false, columnDefinition = "DECIMAL(5, 2)")
    public BigDecimal rate;

    @Column(name = "roi", nullable = false, columnDefinition = "DECIMAL(5, 2)")
    public BigDecimal roi;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    public LoanStatus status = LoanStatus.PROPOSED;

    @Column(name = "agreement_letter_url")
    public String agreementLetterUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

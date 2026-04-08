package com.amartha.loan.domain.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "investor_profiles")
public class InvestorProfile extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(name = "member_id", nullable = false, unique = true)
    public Integer memberId;

    @Column(name = "investment_limit", columnDefinition = "DECIMAL(15, 2)")
    public BigDecimal investmentLimit;

    @Column(name = "risk_appetite_score")
    public Integer riskAppetiteScore;

    @Column(name = "total_balance", columnDefinition = "DECIMAL(19, 2)")
    public BigDecimal totalBalance = BigDecimal.ZERO;

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

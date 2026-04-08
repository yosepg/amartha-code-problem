package com.amartha.loan.domain.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "borrower_profiles")
public class BorrowerProfile extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(name = "member_id", nullable = false, unique = true)
    public Integer memberId;

    @Column(name = "credit_score")
    public Integer creditScore;

    @Column(name = "monthly_income", columnDefinition = "DECIMAL(15, 2)")
    public BigDecimal monthlyIncome;

    @Column(name = "business_category")
    public String businessCategory;

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

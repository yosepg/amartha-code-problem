package com.amartha.loan.domain.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_approvals")
public class LoanApproval extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "loan_id", nullable = false)
    public Long loanId;

    @Column(name = "field_validator_employee_id", nullable = false)
    public Integer fieldValidatorEmployeeId;

    @Column(name = "field_validator_photo_proof_path")
    public String fieldValidatorPhotoProofPath;

    @Column(name = "approval_date", nullable = false)
    public LocalDate approvalDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

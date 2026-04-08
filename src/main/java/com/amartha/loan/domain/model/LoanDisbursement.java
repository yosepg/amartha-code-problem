package com.amartha.loan.domain.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_disbursements")
public class LoanDisbursement extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "loan_id", nullable = false)
    public Long loanId;

    @Column(name = "field_officer_employee_id", nullable = false)
    public Integer fieldOfficerEmployeeId;

    @Column(name = "signed_agreement_letter_path", nullable = false)
    public String signedAgreementLetterPath;

    @Column(name = "disbursement_date", nullable = false)
    public LocalDate disbursementDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

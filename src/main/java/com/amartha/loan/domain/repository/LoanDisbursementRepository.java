package com.amartha.loan.domain.repository;

import com.amartha.loan.domain.model.LoanDisbursement;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class LoanDisbursementRepository implements PanacheRepository<LoanDisbursement> {

    public Optional<LoanDisbursement> findByLoanId(Long loanId) {
        var result = find("loanId", loanId).firstResult().await().indefinitely();
        return result != null ? Optional.of(result) : Optional.empty();
    }
}

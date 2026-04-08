package com.amartha.loan.domain.repository;

import com.amartha.loan.domain.model.LoanApproval;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class LoanApprovalRepository implements PanacheRepository<LoanApproval> {

    public Optional<LoanApproval> findByLoanId(Long loanId) {
        var result = find("loanId", loanId).firstResult().await().indefinitely();
        return result != null ? Optional.of(result) : Optional.empty();
    }

    public Uni<Optional<LoanApproval>> findByLoanIdReactive(Long loanId) {
        return find("loanId", loanId).firstResult()
                .map(result -> result != null ? Optional.of(result) : Optional.empty());
    }
}

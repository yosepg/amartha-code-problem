package com.amartha.loan.domain.repository;

import com.amartha.loan.domain.model.Loan;
import com.amartha.loan.domain.model.LoanStatus;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class LoanRepository implements PanacheRepository<Loan> {

    public Loan findByIdSync(Long id) {
        return find("id", id).firstResult().await().indefinitely();
    }

    public List<Loan> findByStatus(LoanStatus status) {
        return find("status", status).list().await().indefinitely();
    }

    public List<Loan> getAll() {
        return find("1=1").list().await().indefinitely();
    }

    public List<Loan> findByBorrowerId(String borrowerId) {
        return find("borrowerId", borrowerId).list().await().indefinitely();
    }

    public long countByStatus(LoanStatus status) {
        return find("status", status).count().await().indefinitely();
    }
}

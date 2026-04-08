package com.amartha.loan.domain.repository;

import com.amartha.loan.domain.model.LoanInvestment;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.util.List;

@ApplicationScoped
public class LoanInvestmentRepository implements PanacheRepository<LoanInvestment> {

    public List<LoanInvestment> findByLoanId(Long loanId) {
        return find("loanId", loanId).list().await().indefinitely();
    }

    public Uni<List<LoanInvestment>> findByLoanIdReactive(Long loanId) {
        return find("loanId", loanId).list();
    }

    public BigDecimal sumAmountByLoanId(Long loanId) {
        Object result = find("SELECT COALESCE(SUM(li.amount), 0) FROM LoanInvestment li WHERE li.loanId = ?1", loanId)
                .singleResult()
                .await().indefinitely();
        if (result instanceof BigDecimal) {
            return (BigDecimal) result;
        }
        return BigDecimal.ZERO;
    }

    public Uni<BigDecimal> sumAmountByLoanIdReactive(Long loanId) {
        return find("SELECT COALESCE(SUM(li.amount), 0) FROM LoanInvestment li WHERE li.loanId = ?1", loanId)
                .singleResult()
                .map(result -> {
                    Object resultObj = result;
                    if (resultObj == null) {
                        return BigDecimal.ZERO;
                    }
                    // Hibernate returns BigDecimal for SUM queries
                    return (BigDecimal) resultObj;
                });
    }

    public long countByLoanId(Long loanId) {
        return find("loanId", loanId).count().await().indefinitely();
    }
}

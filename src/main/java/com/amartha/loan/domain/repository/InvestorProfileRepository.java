package com.amartha.loan.domain.repository;

import com.amartha.loan.domain.model.InvestorProfile;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class InvestorProfileRepository implements PanacheRepository<InvestorProfile> {

    public Optional<InvestorProfile> findByMemberId(Integer memberId) {
        var result = find("memberId", memberId).firstResult().await().indefinitely();
        return result != null ? Optional.of(result) : Optional.empty();
    }
}

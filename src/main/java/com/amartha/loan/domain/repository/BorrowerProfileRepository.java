package com.amartha.loan.domain.repository;

import com.amartha.loan.domain.model.BorrowerProfile;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class BorrowerProfileRepository implements PanacheRepository<BorrowerProfile> {

    public Optional<BorrowerProfile> findByMemberId(Integer memberId) {
        var result = find("memberId", memberId).firstResult().await().indefinitely();
        return result != null ? Optional.of(result) : Optional.empty();
    }
}

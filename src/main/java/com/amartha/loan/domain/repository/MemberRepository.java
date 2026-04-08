package com.amartha.loan.domain.repository;

import com.amartha.loan.domain.model.Member;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class MemberRepository implements PanacheRepository<Member> {

    public Optional<Member> findByEmail(String email) {
        var result = find("email", email).firstResult().await().indefinitely();
        return result != null ? Optional.of(result) : Optional.empty();
    }

    public Optional<Member> findByNik(String nik) {
        var result = find("nik", nik).firstResult().await().indefinitely();
        return result != null ? Optional.of(result) : Optional.empty();
    }
}

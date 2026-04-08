package com.amartha.loan.domain.repository;

import com.amartha.loan.domain.model.Employee;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class EmployeeRepository implements PanacheRepository<Employee> {

    public List<Employee> findByRole(String role) {
        return find("role", role).list().await().indefinitely();
    }
}

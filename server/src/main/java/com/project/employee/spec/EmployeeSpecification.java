package com.project.employee.spec;

import com.project.employee.model.Employee;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class EmployeeSpecification {
    public static Specification<Employee> withFilter(EmployeeFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getName() != null && !filter.getName().trim().isEmpty()) {
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("firstName")), "%" + filter.getName().toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("lastName")), "%" + filter.getName().toLowerCase() + "%")
                ));
            }

            if (filter.getEmail() != null && !filter.getEmail().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("email")),
                    "%" + filter.getEmail().toLowerCase() + "%"));
            }

            if (filter.getPosition() != null && !filter.getPosition().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("position")),
                    "%" + filter.getPosition().toLowerCase() + "%"));
            }

            if (filter.getPhone() != null && !filter.getPhone().trim().isEmpty()) {
                predicates.add(cb.like(root.get("phone"), "%" + filter.getPhone() + "%"));
            }

            if (filter.getMinSalary() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("salary"), filter.getMinSalary()));
            }

            if (filter.getMaxSalary() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("salary"), filter.getMaxSalary()));
            }

            if (filter.getStartWorkDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startWorkDate"),
                    filter.getStartWorkDateFrom()));
            }

            if (filter.getStartWorkDateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("startWorkDate"),
                    filter.getStartWorkDateTo()));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

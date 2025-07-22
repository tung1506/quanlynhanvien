package com.project.employee.spec;

import com.project.employee.model.User;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class UserSpecification {
    public static Specification<User> withFilter(UserFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getName() != null && !filter.getName().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")),
                    "%" + filter.getName().toLowerCase() + "%"));
            }

            if (filter.getUsername() != null && !filter.getUsername().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("username")),
                    "%" + filter.getUsername().toLowerCase() + "%"));
            }

            if (filter.getRole() != null && !filter.getRole().trim().isEmpty()) {
                predicates.add(cb.equal(root.get("role"), filter.getRole()));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

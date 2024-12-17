package com.mock.user_service.repository;

import com.mock.user_service.model.User;
import org.springframework.data.jpa.domain.Specification;

import java.util.Date;

public class UserSpecification {
    public static Specification<User> activeUser(Boolean isDeleted) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("deleted"), isDeleted);
    }

    public static Specification<User> findUsername(String username) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("username"), "%" + username + "%");
    }

    public static Specification<User> findUsertier(String tier) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("tier"), tier);
    }
    public static Specification<User> findCreateDate(Date createDate) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("createdDateTime"), createDate);
    }
}

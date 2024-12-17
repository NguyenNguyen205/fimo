package com.mock.user_service.repository;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryCustom {
    private EntityManager em;

}

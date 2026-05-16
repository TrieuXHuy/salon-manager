package com.salonnbooking.service;

import org.springframework.stereotype.Service;

import com.salonnbooking.domain.User;
import com.salonnbooking.repository.UserRepository;

import jakarta.persistence.EntityManager;

@Service
public class UserService extends BaseCrudService<User> {

    public UserService(UserRepository repository, EntityManager entityManager) {
        super(repository, entityManager, User.class);
    }
}

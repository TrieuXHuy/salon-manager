package com.salonnbooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salonnbooking.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
}

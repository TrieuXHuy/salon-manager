package com.salonnbooking.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salonnbooking.domain.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, Integer> {
	Optional<UserAccount> findByUsername(String username);
}

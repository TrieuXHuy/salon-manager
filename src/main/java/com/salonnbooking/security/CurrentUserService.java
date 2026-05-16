package com.salonnbooking.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.salonnbooking.domain.User;
import com.salonnbooking.exception.ResourceNotFoundException;
import com.salonnbooking.repository.UserRepository;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User requireCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new ResourceNotFoundException("Current user not found");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof AuthUserPrincipal authUserPrincipal)) {
            throw new ResourceNotFoundException("Current user not found");
        }

        return userRepository.findById(authUserPrincipal.id())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + authUserPrincipal.id()));
    }
}

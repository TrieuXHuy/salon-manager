package com.salonnbooking.security;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.salonnbooking.config.SecurityProperties;
import com.salonnbooking.domain.User;
import com.salonnbooking.repository.AuthTokenRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final AuthTokenRepository authTokenRepository;
    private final SecurityProperties securityProperties;

    public TokenAuthenticationFilter(AuthTokenRepository authTokenRepository, SecurityProperties securityProperties) {
        this.authTokenRepository = authTokenRepository;
        this.securityProperties = securityProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String tokenValue = resolveToken(request);
        if (tokenValue != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            authTokenRepository.findByTokenAndIsActiveTrue(tokenValue)
                    .ifPresent(token -> {
                        if (token.getExpiresAt() != null && token.getExpiresAt().isBefore(LocalDateTime.now())) {
                            token.setIsActive(false);
                            authTokenRepository.save(token);
                            return;
                        }

                        User user = token.getUser();
                        if (!Boolean.TRUE.equals(user.getIsActive())) {
                            return;
                        }

                        token.setLastUsedAt(LocalDateTime.now());
                        authTokenRepository.save(token);
                        authenticate(tokenValue, user);
                    });
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(String tokenValue, User user) {
        AuthUserPrincipal principal = new AuthUserPrincipal(user.getId(), user.getEmail(), user.getRole());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                tokenValue,
                principal.authorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String resolveToken(HttpServletRequest request) {
        String headerName = securityProperties.token().headerName();
        String headerValue = request.getHeader(headerName);
        if (headerValue == null || headerValue.isBlank()) {
            return null;
        }

        if (headerValue.startsWith("Bearer ")) {
            return headerValue.substring(7);
        }
        return headerValue;
    }
}

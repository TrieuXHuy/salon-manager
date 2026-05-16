package com.salonnbooking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

import com.salonnbooking.security.TokenAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, TokenAuthenticationFilter tokenAuthenticationFilter)
            throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/register",
                                "/api/auth/login")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register-admin")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/auth/me", "/api/account/**")
                        .authenticated()
                        .requestMatchers("/api/booking/**", "/api/customer/**")
                        .hasRole("CUSTOMER")
                        .requestMatchers("/api/staff/**")
                        .hasRole("STAFF")
                        .requestMatchers(HttpMethod.POST, "/api/appointments")
                        .hasRole("CUSTOMER")
                        .requestMatchers("/api/users/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/services/**")
                        .permitAll()
                        .requestMatchers(
                                "/api/staff-services/**",
                                "/api/staff-working-hours/**")
                        .hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers(
                                "/api/appointment-services/**",
                                "/api/payments/**",
                                "/api/reviews/**")
                        .hasAnyRole("ADMIN", "STAFF", "CUSTOMER")
                        .anyRequest()
                        .permitAll())
                .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

package com.marko.anime.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.http.HttpMethod.GET;

@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/anime", "/api/v1/auth/**").permitAll()
                        .requestMatchers(GET,"/api/v1/anime/*").authenticated()
                        .requestMatchers("/api/v1/anime/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/anime-reviews/**").authenticated()
                        .requestMatchers("/api/v1/users", "/api/v1/users/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
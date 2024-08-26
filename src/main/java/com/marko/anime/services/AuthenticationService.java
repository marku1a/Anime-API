package com.marko.anime.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marko.anime.dtos.AuthenticationRequest;
import com.marko.anime.dtos.AuthenticationResponse;
import com.marko.anime.dtos.RegisterRequest;
import com.marko.anime.exceptions.EmailAlreadyInUseException;
import com.marko.anime.exceptions.RefreshTokenInProgressException;
import com.marko.anime.exceptions.UserIdAlreadyInUseException;
import com.marko.anime.models.Role;
import com.marko.anime.models.Token;
import com.marko.anime.models.TokenType;
import com.marko.anime.models.User;
import com.marko.anime.repositories.TokenRepository;
import com.marko.anime.repositories.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final Set<String> refreshTokenInProgress = new HashSet<>();


    public AuthenticationResponse register(RegisterRequest request) {
            validateRegistrationRequest(request);
            var user = User.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .email(request.getEmail().trim().toLowerCase())
                    .userId(request.getUserId().trim())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role("ROLE_" + Role.USER)
                    .accountNonLocked(true)
                    .build();
            var savedUser = userRepository.save(user);
            return AuthenticationResponse.builder()
                    .userId(savedUser.getUserId())
                    .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
            var user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new BadCredentialsException(""));

                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
                );

            var jwtToken = jwtService.generateToken(user);
            var refreshToken = jwtService.generateRefreshToken(user);
            revokeAllUserTokens(user);
            saveUserToken(user, jwtToken);
            return AuthenticationResponse.builder()
                    .accessToken(jwtToken)
                    .refreshToken(refreshToken)
                    .userId(user.getUserId())
                    .role(user.getRole())
                    .build();
    }

    private void saveUserToken(User user, String jwtToken) {
        List<Token> existingTokens = tokenRepository.findAllValidTokenByUser(user.getId(), false, false);
        existingTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(existingTokens);
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(String.valueOf(TokenType.BEARER))
                .revoked(false)
                .expired(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId(), false, false);
        if (!validUserTokens.isEmpty()) {
            validUserTokens.forEach(token -> {
                token.setExpired(true);
                token.setRevoked(true);
            });
        }
        tokenRepository.saveAll(validUserTokens);
    }

    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String refreshToken = extractRefreshToken(request);
        if (refreshToken == null ){
            throw new IllegalArgumentException("Refresh token not found");
        }
        final String userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            if (!refreshTokenInProgress.add(userEmail)) {
                throw new RefreshTokenInProgressException("Refresh token operation in progress");
            }
            try {
                var user = this.userRepository.findByEmail(userEmail)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
                if (jwtService.isTokenValid(refreshToken, user)) {
                    var accessToken = jwtService.generateToken(user);
                    revokeAllUserTokens(user);
                    saveUserToken(user, accessToken);
                    var authResponse = AuthenticationResponse.builder()
                            .accessToken(accessToken)
                            .refreshToken(refreshToken)
                            .userId(user.getUserId())
                            .role(user.getRole())
                            .build();
                    setRTokenAsCookie(response, refreshToken);
                    new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
                } else {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid refresh token");
                }
            } finally {
                refreshTokenInProgress.remove(userEmail);
            }
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User email not found");
        }
    }


    String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refresh_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public void setRTokenAsCookie(HttpServletResponse response, String token) {
        long refreshExp = jwtService.getRefreshExpiration();
        ResponseCookie cookie = ResponseCookie.from("refresh_token", token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(Duration.ofMillis(refreshExp))
                .build();
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
    void validateRegistrationRequest(RegisterRequest request) {
        if (!StringUtils.hasText(request.getFirstName()) ||
                !StringUtils.hasText(request.getLastName()) ||
                !StringUtils.hasText(request.getEmail()) ||
                !StringUtils.hasText(request.getPassword()) ||
                !StringUtils.hasText(request.getUserId())) {
            throw new IllegalArgumentException("Missing required fields.");
        }
        if (!request.getFirstName().matches("^[a-zA-Z]+$")) {
            throw new IllegalArgumentException("First name must contain only letters.");
        }
        if (!request.getLastName().matches("^[a-zA-Z]+$")) {
            throw new IllegalArgumentException("Last name must contain only letters.");
        }
        if (!request.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        if (!request.getUserId().matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Username must contain only letters, numbers, and underscores.");
        }
        if (!request.getPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d|.*[!@#$%^&*()_+{}:\"<>?\\[\\];',./`~\\\\|-]).{6,}$")) {
            throw new IllegalArgumentException("Password must contain at least 6 characters, one uppercase letter, one lowercase letter, and one number or special character.");
        }
        if (userRepository.findByEmail(request.getEmail().trim().toLowerCase()).isPresent()) {
            throw new EmailAlreadyInUseException("This e-mail is already in use!");
        }
        if (userRepository.findByUserIdIgnoreCase(request.getUserId().trim().toLowerCase()).isPresent()) {
            throw new UserIdAlreadyInUseException("This username is already in use!");
        }
    }
}

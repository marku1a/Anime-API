package com.marko.anime.services;

import com.marko.anime.models.Token;
import com.marko.anime.repositories.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private LogoutService logoutService;

    private String jwt;
    private Token storedToken;

    @BeforeEach
    void init() {
        jwt = "test.jwt.token";
        storedToken = new Token();
        storedToken.setToken(jwt);
        storedToken.setExpired(false);
        storedToken.setRevoked(false);
    }

    @Test
    void logout_ShouldDoNothing_WhenAuthHeaderIsMissing() {
        when(request.getHeader("Authorization")).thenReturn(null);

        logoutService.logout(request, response, authentication);

        verify(tokenRepository, never()).findByToken(any());
        verify(response, never()).setHeader(eq(HttpHeaders.SET_COOKIE), anyString());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void logout_ShouldDoNothing_WhenAuthHeaderDoesNotStartWithBearer() {
        when(request.getHeader("Authorization")).thenReturn("InvalidAuthHeader");

        logoutService.logout(request, response, authentication);

        verify(tokenRepository, never()).findByToken(any());
        verify(response, never()).setHeader(eq(HttpHeaders.SET_COOKIE), anyString());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void logout_ShouldDoNothing_WhenTokenIsNotFound() {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(tokenRepository.findByToken(jwt)).thenReturn(Optional.empty());

        logoutService.logout(request, response, authentication);

        verify(tokenRepository, times(1)).findByToken(jwt);
        verify(tokenRepository, never()).save(any());

        verify(response, times(1)).setHeader(eq(HttpHeaders.SET_COOKIE), anyString());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void logout_ShouldExpireAndRevokeToken_WhenTokenExists() {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(tokenRepository.findByToken(jwt)).thenReturn(Optional.of(storedToken));

        logoutService.logout(request, response, authentication);

        assertThat(storedToken.isExpired()).isTrue();
        assertThat(storedToken.isRevoked()).isTrue();
        verify(tokenRepository, times(1)).findByToken(jwt);
        verify(tokenRepository, times(1)).save(storedToken);

        verify(response, times(1)).setHeader(eq(HttpHeaders.SET_COOKIE), anyString());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}

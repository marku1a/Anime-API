package com.marko.anime.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marko.anime.configuration.TestSecurityConfig;
import com.marko.anime.dtos.AuthenticationRequest;
import com.marko.anime.dtos.AuthenticationResponse;
import com.marko.anime.dtos.RegisterRequest;
import com.marko.anime.exceptions.EmailAlreadyInUseException;
import com.marko.anime.exceptions.ErrorResponse;
import com.marko.anime.repositories.TokenRepository;
import com.marko.anime.services.AuthenticationService;
import com.marko.anime.services.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Fail.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(AuthenticationController.class)
@Import(TestSecurityConfig.class)
class AuthenticationControllerTest {

    @MockBean
    private AuthenticationService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private TokenRepository tokenRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void register_shouldReturnStatusOk_whenSuccessful() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .firstName("John")
                .lastName("Wick")
                .email("john.wick@continental.com")
                .userId("johnW")
                .password("password123")
                .role("ROLE_USER")
                .build();

        AuthenticationResponse authResponse = AuthenticationResponse.builder()
                .accessToken("test.access.token")
                .refreshToken("test.refresh.token")
                .userId("johnW")
                .role("ROLE_USER")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(authResponse)));

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void register_shouldReturnBadRequest_whenEmailAlreadyExists() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .firstName("John")
                .lastName("Wick")
                .email("john.wick@continental.com")
                .userId("johnW")
                .password("password123")
                .role("ROLE_USER")
                .build();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(400)
                .message("This e-mail is already in use!")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenThrow(new EmailAlreadyInUseException("This e-mail is already in use!"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(errorResponse)));

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void authenticate_shouldReturnStatusOk_whenSuccessful() throws Exception {
        AuthenticationRequest authRequest = AuthenticationRequest.builder()
                .email("john.wick@continental.com")
                .password("password123")
                .build();

        AuthenticationResponse authResponse = AuthenticationResponse.builder()
                .accessToken("test.access.token")
                .refreshToken("test.refresh.token")
                .userId("johnW")
                .role("ROLE_USER")
                .build();

        when(authService.authenticate(any(AuthenticationRequest.class))).thenReturn(authResponse);
        doNothing().when(authService).setRTokenAsCookie(any(HttpServletResponse.class), anyString());

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(authResponse)));

        verify(authService).authenticate(any(AuthenticationRequest.class));
        verify(authService).setRTokenAsCookie(any(HttpServletResponse.class), anyString());
    }

    @Test
    void authenticate_shouldBeForbidden_whenBadCredentials() throws Exception {
        AuthenticationRequest authRequest = AuthenticationRequest.builder()
                .email("johnW@continental.com")
                .password("wrongpassword")
                .build();
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(403)
                .message("Login failed: Check your email and password and try again.")
                .build();

        when(authService.authenticate(any(AuthenticationRequest.class))).thenThrow(new BadCredentialsException(""));

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isForbidden())
                .andExpect(content().json(objectMapper.writeValueAsString(errorResponse)));

        verify(authService).authenticate(any(AuthenticationRequest.class));
    }

    @Test
    void authenticate_shouldBeForbidden_whenAccountLocked() throws Exception {
        AuthenticationRequest authRequest = AuthenticationRequest.builder()
                .email("banned@continental.com")
                .password("password123")
                .build();
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(403)
                .message("This account is banned!")
                .build();

        when(authService.authenticate(any(AuthenticationRequest.class))).thenThrow(new LockedException("This account is banned!"));

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isForbidden())
                .andExpect(content().json(objectMapper.writeValueAsString(errorResponse)));

        verify(authService).authenticate(any(AuthenticationRequest.class));
    }

    @Test
    void refreshToken_shouldReturnStatusOk_WhenSuccessful() throws Exception {
        doNothing().when(authService).refreshToken(any(HttpServletRequest.class), any(HttpServletResponse.class));

        mockMvc.perform(post("/api/v1/auth/refresh-token"))
                .andExpect(status().isOk());

        verify(authService).refreshToken(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    void refreshToken_shouldReturnBadRequest_whenRefreshTokenNotFound() throws Exception {
        doAnswer(invocation -> {
            HttpServletResponse response = invocation.getArgument(1);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Refresh token not found");
            return null;
        }).when(authService).refreshToken(any(HttpServletRequest.class), any(HttpServletResponse.class));

        mockMvc.perform(post("/api/v1/auth/refresh-token"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    if (!responseBody.isEmpty()) {
                        fail("Expected empty response body but got: " + responseBody);
                    }
                });

        verify(authService).refreshToken(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }
}
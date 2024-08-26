package com.marko.anime.services;

import com.marko.anime.dtos.AuthenticationRequest;
import com.marko.anime.dtos.AuthenticationResponse;
import com.marko.anime.dtos.RegisterRequest;
import com.marko.anime.exceptions.EmailAlreadyInUseException;
import com.marko.anime.exceptions.UserIdAlreadyInUseException;
import com.marko.anime.models.Token;
import com.marko.anime.models.TokenType;
import com.marko.anime.models.User;
import com.marko.anime.repositories.TokenRepository;
import com.marko.anime.repositories.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenRepository tokenRepository;


    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authenticationService;

    private RegisterRequest validRegisterRequest;

    @BeforeEach
    void init() {
        validRegisterRequest = RegisterRequest.builder()
                .firstName("John")
                .lastName("Wick")
                .email("john.wick@mail.com")
                .userId("john_W")
                .password("password")
                .role("ROLE_USER")
                .build();
    }
    // Register tests

    @Test
    void register_shouldThrowException_WhenRequiredFieldsAreMissing() {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("")
                .lastName("")
                .email("")
                .userId("")
                .password("")
                .role("ROLE_USER")
                .build();


        assertThatThrownBy(() -> authenticationService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Missing required fields");
    }
    @Test
    void register_shouldThrowException_whenEmailIsAlreadyInUse() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authenticationService.register(validRegisterRequest))
                .isInstanceOf(EmailAlreadyInUseException.class)
                .hasMessageContaining("This e-mail is already in use!");
    }
    @Test
    void register_shouldThrowException_whenUsernameIsAlreadyInUse() {
        when(userRepository.findByUserIdIgnoreCase(anyString())).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authenticationService.register(validRegisterRequest))
                .isInstanceOf(UserIdAlreadyInUseException.class)
                .hasMessageContaining("This username is already in use!");
    }
    @Test
    void register_shouldRegisterUser_whenValidRequest() {
        User savedUser = User.builder()
                .firstName("John")
                .lastName("Wick")
                .email("john.wick@mail.com")
                .userId("john_W")
                .password("password")
                .role("ROLE_USER")
                .accountNonLocked(true)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        AuthenticationResponse response = authenticationService.register(validRegisterRequest);

        assertThat(response).isNotNull();

    }

    // Authenticate tests

    @Test
    void authenticate_shouldThrowException_whenEmailNotFound() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("john.wick@mail.com")
                .password("password")
                .build();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.authenticate(request))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Email not found!");
    }
    @Test
    void authenticate_shouldThrowException_whenWrongPassword() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("john.wick@mail.com")
                .password("password")
                .build();
        User user = User.builder()
                .email("john.wick@mail.com")
                .password("")
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        doThrow(new BadCredentialsException("Bad credentials!"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThatThrownBy(() -> authenticationService.authenticate(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Bad credentials!");
    }
    @Test
    void authenticate_shouldAuthenticateUser_whenValidRequest() {
        User user = User.builder()
                .firstName("John")
                .lastName("Wick")
                .email("john.wick@mail.com")
                .userId("john_W")
                .password("encodedPassword")
                .role("ROLE_USER")
                .accountNonLocked(true)
                .build();

        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("john.wick@mail.com")
                .password("password")
                .build();

        Token existingToken = Token.builder()
                .user(user)
                .token("existingToken")
                .tokenType(String.valueOf(TokenType.BEARER))
                .revoked(false)
                .expired(false)
                .build();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwtToken");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refreshToken");
        when(tokenRepository.findAllValidTokenByUser(eq(user.getId()), eq(false), eq(false)))
                .thenReturn(List.of(existingToken));

        AuthenticationResponse response = authenticationService.authenticate(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("jwtToken");
        assertThat(response.getRefreshToken()).isEqualTo("refreshToken");
        assertThat(response.getUserId()).isEqualTo("john_W");
        assertThat(response.getRole()).isEqualTo("ROLE_USER");
        assertThat(existingToken.isRevoked()).isTrue();
        assertThat(existingToken.isExpired()).isTrue();
        verify(tokenRepository, times(2)).saveAll(anyList()); //revokeAllUserTokens & saveUserToken
        verify(tokenRepository, times(1)).save(any(Token.class)); //saveUserToken
    }

    // refreshToken tests

    @Test
    void refreshToken_shouldRefreshToken_whenValidRequest() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String refreshTokenValue = "validRefreshToken";
        String userEmail = "john.wick@mail.com";
        User user = User.builder()
                .firstName("John")
                .lastName("Wick")
                .email(userEmail)
                .userId("john_W")
                .password("encodedPassword")
                .role("ROLE_USER")
                .accountNonLocked(true)
                .build();

        request.setCookies(new Cookie("refresh_token", refreshTokenValue));


        when(jwtService.extractUsername(refreshTokenValue)).thenReturn(userEmail);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid(refreshTokenValue, user)).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwtToken");

        authenticationService.refreshToken(request, response);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        assertThat(response.getContentAsString()).isNotEmpty();
        verify(jwtService, times(1)).generateToken(user);
        verify(tokenRepository, times(2)).saveAll(any());
        verify(tokenRepository, times(1)).save(any());
    }
    @Test
    void refreshToken_shouldReturnUnauthorized_whenInvalidToken() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String refreshTokenValue = "invalidRefreshToken";
        String userEmail = "john.wick@mail.com";

        request.setCookies(new Cookie("refresh_token", refreshTokenValue));

        when(jwtService.extractUsername(refreshTokenValue)).thenReturn(userEmail);

        User user = new User();
        user.setEmail(userEmail);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        when(jwtService.isTokenValid(refreshTokenValue, user)).thenReturn(false);

        authenticationService.refreshToken(request, response);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getErrorMessage()).isEqualTo("Invalid refresh token");
        verify(jwtService, never()).generateToken(any());
    }
}





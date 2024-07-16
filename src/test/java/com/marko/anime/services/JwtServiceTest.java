package com.marko.anime.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void init() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", "233434732b3f526a6b5e40704b36275773426632547663217254225165713472");

        userDetails = new User("john.wick@mail.com", "password", new ArrayList<>());
    }

    @Test
    void extractUsername_shouldReturnCorrectUsername() {
        String token = jwtService.generateToken(userDetails);

        assertThat(jwtService.extractUsername(token)).isEqualTo("john.wick@mail.com");
    }

    @Test
    void generateToken_shouldReturnValidToken() {
        String token = jwtService.generateToken(userDetails);

        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void generateToken_WithExtraClaims_shouldIncludeExtraClaims() {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("key", "value");
        String token = jwtService.generateToken(extraClaims, userDetails);

        String extractedValue = jwtService.extractClaim(token, claims -> claims.get("key", String.class));

        assertThat(extractedValue).isEqualTo("value");

    }

    @Test
    void generateRefreshToken_shouldGenerateValidRefreshTokenWithOneWeekExpiration() {
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        Date expirationDate = jwtService.extractClaim(refreshToken, Claims::getExpiration);
        long expirationTime = expirationDate.getTime() - System.currentTimeMillis();

        assertThat(jwtService.isTokenValid(refreshToken, userDetails)).isTrue();
        assertThat(expirationTime).isCloseTo(604800000L, Offset.offset(1000L));
    }

    @Test
    void isTokenExpired_extractExpiration_shouldNotBeExpired() {
        String token = jwtService.generateToken(userDetails);

        Date expiration = jwtService.extractClaim(token, Claims::getExpiration);

        assertThat(expiration).isAfter(new Date());
    }

    @Test
    void isTokenValid_shouldReturnFalseForInvalidUser() {
        String token = jwtService.generateToken(userDetails);
        UserDetails anotherUser = new User("johnny.bravo@mail.com", "password", new ArrayList<>());

        assertThat(jwtService.isTokenValid(token, anotherUser)).isFalse();
    }

    @Test
    void isTokenValid_shouldThrowExceptionForExpiredToken() {
        String expiredToken = Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis() - 100000))
                .expiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(ReflectionTestUtils.invokeMethod(jwtService, "getSignInKey"))
                .compact();

        assertThatThrownBy(() -> jwtService.isTokenValid(expiredToken, userDetails))
                .isInstanceOf(ExpiredJwtException.class);

    }
}

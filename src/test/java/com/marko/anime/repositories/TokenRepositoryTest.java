package com.marko.anime.repositories;

import com.marko.anime.models.Token;
import com.marko.anime.models.User;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@TestPropertySource(locations = "classpath:application-test.properties")
class TokenRepositoryTest {

    @Autowired
    private TokenRepository tokenRepository;

    @AfterEach
    void tearDown() {
        tokenRepository.deleteAll();
    }

    @Test
    void save_shouldReturnSavedToken() {
        Token token = Token.builder()
                .token("test_token")
                .tokenType("BEARER")
                .revoked(false)
                .expired(false)
                .build();

        Token savedToken = tokenRepository.save(token);

        assertThat(savedToken).isNotNull();
        assertThat(savedToken.getId()).isNotNull();
    }

    @Test
    void findByToken_shouldReturnToken() {
        Token token = Token.builder()
                .token("test_token")
                .tokenType("BEARER")
                .revoked(false)
                .expired(false)
                .build();
        tokenRepository.save(token);

        Optional<Token> foundToken = tokenRepository.findByToken("test_token");

        assertThat(foundToken).isPresent();
        assertThat(foundToken.get().getToken()).isEqualTo("test_token");
    }

    @Test
    void findByToken_shouldReturnEmptyForNonExistingToken() {
        Optional<Token> foundToken = tokenRepository.findByToken("nonExistingToken");

        assertThat(foundToken).isEmpty();
    }

    @Test
    void findAllValidTokenByUser_shouldReturnValidTokens() {
        ObjectId userId = new ObjectId();
        Token expiredToken = Token.builder()
                .token("expiredToken")
                .tokenType("BEARER")
                .revoked(false)
                .expired(true)
                .user(User.builder().id(userId).build())
                .build();
        Token validToken = Token.builder()
                .token("validToken")
                .tokenType("BEARER")
                .revoked(false)
                .expired(false)
                .user(User.builder().id(userId).build())
                .build();

        tokenRepository.saveAll(Arrays.asList(expiredToken, validToken));

        List<Token> validTokens = tokenRepository.findAllValidTokenByUser(userId, false, false);

        assertThat(validTokens).hasSize(1)
                .allMatch(token -> !token.isRevoked() && !token.isExpired());
    }
    @Test
    void deleteByExpired_shouldDeleteExpiredReturnNonExpiredToken() {
        ObjectId userId = new ObjectId();
        Token expiredToken = Token.builder()
                .token("expiredToken")
                .tokenType("BEARER")
                .revoked(false)
                .expired(true)
                .user(User.builder().id(userId).build())
                .build();
        Token validToken = Token.builder()
                .token("validToken")
                .tokenType("BEARER")
                .revoked(false)
                .expired(false)
                .user(User.builder().id(userId).build())
                .build();
        tokenRepository.saveAll(List.of(expiredToken, validToken));
        tokenRepository.deleteByExpired(true);
        List<Token> tokens = tokenRepository.findAll();

        assertThat(tokens).hasSize(1).allMatch(token -> !token.isExpired());
    }
}
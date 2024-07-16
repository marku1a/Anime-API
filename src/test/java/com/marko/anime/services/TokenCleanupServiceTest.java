package com.marko.anime.services;

import com.marko.anime.repositories.TokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TokenCleanupServiceTest {

    @Mock
    TokenRepository tokenRepository;

    @InjectMocks
    TokenCleanupService tokenCleanupService;

    @Test
    void deleteExpiredTokens_shouldDeleteExpiredTokens() {
        tokenCleanupService.deleteExpiredTokens();

        assertThat(tokenRepository.findAllTokenByExpiredTrue()).isEmpty();

        verify(tokenRepository, times(1)).deleteByExpired(true);
    }

}

package com.marko.anime.services;


import com.marko.anime.repositories.TokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TokenCleanupService {

    private final TokenRepository tokenRepository;
    public TokenCleanupService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Scheduled(cron = "0 0 5 * * ?")
    public void deleteExpiredTokens() {
        tokenRepository.deleteByExpired(true);
    }
}

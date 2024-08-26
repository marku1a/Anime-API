package com.marko.anime.exceptions;

public class RefreshTokenInProgressException extends RuntimeException {
    public RefreshTokenInProgressException(String message) {
        super(message);
    }
}

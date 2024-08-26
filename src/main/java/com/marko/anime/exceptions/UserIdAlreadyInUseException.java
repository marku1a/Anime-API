package com.marko.anime.exceptions;

public class UserIdAlreadyInUseException extends RuntimeException {
    public UserIdAlreadyInUseException(String message) {
        super(message);
    }
}

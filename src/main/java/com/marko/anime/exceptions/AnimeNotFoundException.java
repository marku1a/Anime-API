package com.marko.anime.exceptions;

public class AnimeNotFoundException extends RuntimeException{

    public AnimeNotFoundException() {
        super("Anime not found.");
    }

    public AnimeNotFoundException(String message){
        super(message);
    };
}

package com.marko.anime.services;

import com.marko.anime.models.Anime;
import com.marko.anime.repositories.AnimeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AnimeService {

    private final AnimeRepository animeRepository;
    public AnimeService(AnimeRepository animeRepository) {
        this.animeRepository = animeRepository;
    }

    public List<Anime> findAllAnime() {
        return animeRepository.findAll();
    }
    public Optional<Anime> findAnimeByImdbId(String id) {
        return animeRepository.findAnimeByImdbId(id);
    }
}

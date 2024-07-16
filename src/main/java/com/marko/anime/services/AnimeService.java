package com.marko.anime.services;

import com.marko.anime.models.Anime;
import com.marko.anime.repositories.AnimeRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class AnimeService {

    private final AnimeRepository animeRepository;

    public AnimeService(AnimeRepository animeRepository) {
        this.animeRepository = animeRepository;
    }

    public List<Anime> findAllAnime() {
        try {
            return animeRepository.findAll();
        } catch (DataAccessException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Database error occurred while retrieving anime list.", e);
        }
    }
    public Optional<Anime> findAnimeByImdbId(String id) {
        Optional<Anime> animeOptional = animeRepository.findAnimeByImdbId(id);
        if (animeOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Anime not found");
        }
        return animeOptional;
    }
    public Anime createNewAnime(Anime anime) {
        try {
            return animeRepository.save(anime);
        } catch (DataAccessException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Database error occurred while creating new anime.", e);
        }
    }
    public Anime updateAnime(String id, Anime anime) {
        try {
            Optional<Anime> existingAnimeOptional = animeRepository.findAnimeByImdbId(id);

            if (existingAnimeOptional.isPresent()) {
                Anime existingAnime = existingAnimeOptional.get();

                existingAnime.setTitle(anime.getTitle());
                existingAnime.setImdbId(anime.getImdbId());
                existingAnime.setReleaseDate(anime.getReleaseDate());
                existingAnime.setTrailer(anime.getTrailer());
                existingAnime.setPoster(anime.getPoster());
                existingAnime.setGenres(anime.getGenres());
                existingAnime.setBackdrops(anime.getBackdrops());

                return animeRepository.save(existingAnime);
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Anime not found");
            }
        } catch (DataAccessException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Database error occurred while updating anime.", e);
        }
    }
    public void deleteAnime(String id) {
        try {
            Optional<Anime> opt = animeRepository.deleteAnimeByImdbId(id);
            if (opt.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Anime not found!");
            }
        } catch (DataAccessException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Database error occurred while deleting anime.", e);
        }
    }
}

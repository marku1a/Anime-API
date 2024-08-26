package com.marko.anime.services;

import com.marko.anime.exceptions.AnimeNotFoundException;
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
        Optional<Anime> animeOptional = animeRepository.findAnimeByImdbId(id);
        if (animeOptional.isEmpty()) {
            throw new AnimeNotFoundException();
        }
        return animeOptional;
    }

    public Anime createNewAnime(Anime anime) {
            return animeRepository.save(anime);
    }

    public Anime updateAnime(String id, Anime anime) {
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
                throw new AnimeNotFoundException();
            }
    }

    public void deleteAnime(String id) {
            Optional<Anime> opt = animeRepository.deleteAnimeByImdbId(id);
            if (opt.isEmpty()) {
                throw new AnimeNotFoundException();
            }
    }
}

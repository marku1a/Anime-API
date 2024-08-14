package com.marko.anime.repositories;

import com.marko.anime.models.Anime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@TestPropertySource(locations = "classpath:application-test.properties")
class AnimeRepositoryTest {

    @Autowired
    private AnimeRepository animeRepository;

    @AfterEach
    void tearDown() {
        animeRepository.deleteAll();
    }

    @Test
    void save_shouldReturnSavedAnime() {
        Anime anime = Anime.builder()
                .imdbId("tt123456")
                .title("My Anime")
                .build();

        Anime savedAnime = animeRepository.save(anime);

        assertThat(savedAnime).isNotNull();
        assertThat(savedAnime.getId()).isNotNull();
    }

    @Test
    void findAnimeByImdbId_shouldReturnAnime() {
        Anime anime = Anime.builder()
                .imdbId("tt123456")
                .title("My Anime")
                .build();
        animeRepository.save(anime);

        Optional<Anime> foundAnime = animeRepository.findAnimeByImdbId("tt123456");

        assertThat(foundAnime).isPresent();
        assertThat(foundAnime.get().getTitle()).isEqualTo("My Anime");
    }

    @Test
    void findAnimeByImdbId_shouldReturnEmptyForNonExistingAnime() {
        Optional<Anime> foundAnime = animeRepository.findAnimeByImdbId("nonExistingId");

        assertThat(foundAnime).isEmpty();
    }

    @Test
    void deleteAnimeByImdbId_shouldDeleteAnime() {
        Anime anime = Anime.builder()
                .imdbId("tt123456")
                .title("My Anime")
                .build();
        animeRepository.save(anime);

        Optional<Anime> deletedAnime = animeRepository.deleteAnimeByImdbId("tt123456");

        assertThat(deletedAnime).isPresent();
        assertThat(animeRepository.findAnimeByImdbId("tt123456")).isEmpty();
    }

    @Test
    void deleteAnimeByImdbId_shouldReturnEmptyForNonExistingAnime() {

        Optional<Anime> deletedAnime = animeRepository.deleteAnimeByImdbId("nonExistingId");

        assertThat(deletedAnime).isEmpty();
    }
}

package com.marko.anime.services;

import com.marko.anime.models.Anime;
import com.marko.anime.repositories.AnimeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnimeServiceTest {

    @Mock
    private AnimeRepository animeRepository;

    @InjectMocks
    private AnimeService animeService;

    @Test
    void findAllAnime_shouldReturnListOfAnime() {
        List<Anime> animeList = Collections.singletonList(Anime.builder().build());
        when(animeRepository.findAll()).thenReturn(animeList);

        assertThat(animeList).isEqualTo(animeService.findAllAnime());
        verify(animeRepository, times(1)).findAll();
    }

    @Test
    void findAllAnime_shouldThrowException_whenDatabaseError() {
        when(animeRepository.findAll()).thenThrow(new DataAccessException("Database error") {});

        assertThrows(ResponseStatusException.class, () -> animeService.findAllAnime());

        verify(animeRepository, times(1)).findAll();
    }

    @Test
    void findAnimeByImdbId_shouldReturnAnime_whenFound() {
        String imdbId = "tt1234567";
        Anime anime = Anime.builder().imdbId(imdbId).build();
        when(animeRepository.findAnimeByImdbId(imdbId)).thenReturn(Optional.of(anime));

        Optional<Anime> returned = animeService.findAnimeByImdbId(imdbId);

        assertThat(returned).isNotEmpty().contains(anime);

        verify(animeRepository, times(1)).findAnimeByImdbId(imdbId);
    }

    @Test
    void findAnimeByImdbId_shouldThrowException_whenNotFound() {
        String imdbId = "tt1234567";
        when(animeRepository.findAnimeByImdbId(imdbId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> animeService.findAnimeByImdbId(imdbId));

        verify(animeRepository, times(1)).findAnimeByImdbId(imdbId);
    }

    @Test
    void createNewAnime_shouldSaveAnimeSuccessfully() {
        Anime anime = Anime.builder().build();
        when(animeRepository.save(anime)).thenReturn(anime);

        Anime saved = animeRepository.save(anime);
        assertThat(anime).isEqualTo(saved);

        verify(animeRepository, times(1)).save(anime);
    }

    @Test
    void createNewAnime_shouldThrowException_whenDatabaseError() {
        Anime anime = Anime.builder().build();
        when(animeRepository.save(anime)).thenThrow(new DataAccessException("Database error") {});

        assertThrows(ResponseStatusException.class, () -> animeService.createNewAnime(anime));

        verify(animeRepository, times(1)).save(anime);
    }

    @Test
    void updateAnime_shouldUpdateAnime_whenFound() {
        String imdbId = "tt1234567";
        Anime anime = Anime.builder().imdbId(imdbId).build();

        when(animeRepository.findAnimeByImdbId(imdbId)).thenReturn(Optional.of(anime));
        when(animeRepository.save(anime)).thenReturn(anime);

        Anime updated = animeService.updateAnime(imdbId, anime);
        assertThat(anime).isEqualTo(updated);

        verify(animeRepository, times(1)).findAnimeByImdbId(imdbId);
        verify(animeRepository, times(1)).save(any(Anime.class));
    }

    @Test
    void updateAnime_shouldThrowException_whenNotFound() {
        String imdbId = "tt1234567";
        Anime anime = Anime.builder().imdbId(imdbId).build();
        when(animeRepository.findAnimeByImdbId(imdbId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> animeService.updateAnime(imdbId, anime));
        verify(animeRepository, times(1)).findAnimeByImdbId(imdbId);
    }


    @Test
    void deleteAnime_shouldDeleteAnime_whenFound() {
        String imdbId = "tt1234567";
        Anime anime = Anime.builder().imdbId(imdbId).build();
        when(animeRepository.deleteAnimeByImdbId(imdbId)).thenReturn(Optional.of(anime));

        animeService.deleteAnime(imdbId);
        verify(animeRepository, times(1)).deleteAnimeByImdbId(imdbId);
    }

    @Test
    void deleteAnime_shouldThrowException_whenNotFound() {
        String imdbId = "tt1234567";
        when(animeRepository.deleteAnimeByImdbId(imdbId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> animeService.deleteAnime(imdbId));
        verify(animeRepository, times(1)).deleteAnimeByImdbId(imdbId);
    }

    @Test
    void deleteAnime_shouldThrowException_whenDatabaseError() {
        String imdbId = "tt1234567";
        when(animeRepository.deleteAnimeByImdbId(imdbId)).thenThrow(new DataAccessException("Database error") {
        });

        assertThrows(ResponseStatusException.class, () -> animeService.deleteAnime(imdbId));
        verify(animeRepository, times(1)).deleteAnimeByImdbId(imdbId);
    }
}

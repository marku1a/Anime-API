package com.marko.anime.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marko.anime.configuration.TestSecurityConfig;
import com.marko.anime.models.Anime;
import com.marko.anime.repositories.TokenRepository;
import com.marko.anime.services.AnimeService;
import com.marko.anime.services.JwtService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
@WebMvcTest(AnimeController.class)
@Import(TestSecurityConfig.class)
class AnimeControllerTest {

    @MockBean
    private AnimeService animeService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private TokenRepository tokenRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Anime anime;

    @BeforeEach
    void init() {
        ObjectId id = new ObjectId();
        anime = Anime.builder()
                .id(id)
                .imdbId("tt123123")
                .title("Test Anime")
                .releaseDate("05.05.2020")
                .trailer("yt.com")
                .poster("poster.img")
                .genres(List.of("Genre"))
                .backdrops(List.of("backdrop.img"))
                .reviewIds(List.of())
                .build();
    }

    @Test
    @WithMockUser
    void getAllAnime_shouldReturnListOfAnime() throws Exception {
        List<Anime> animeList = List.of(anime);
        when(animeService.findAllAnime()).thenReturn(animeList);

        mockMvc.perform(get("/api/v1/anime"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(animeList)));

        verify(animeService, times(1)).findAllAnime();
    }

    @Test
    @WithMockUser
    void getAnimeById_shouldReturnAnime() throws Exception {
        when(animeService.findAnimeByImdbId("tt123123")).thenReturn(Optional.of(anime));

        mockMvc.perform(get("/api/v1/anime/tt123123"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(Optional.of(anime))));

        verify(animeService, times(1)).findAnimeByImdbId("tt123123");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAnime_shouldReturnCreated() throws Exception {
        when(animeService.createNewAnime(any(Anime.class))).thenReturn(anime);

        mockMvc.perform(post("/api/v1/anime/create-anime")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(anime)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(anime)));

        verify(animeService, times(1)).createNewAnime(any(Anime.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateAnime_shouldReturnStatusOk() throws Exception {
        Anime updatedAnime = Anime.builder()
                .imdbId("tt123123")
                .title("Updated Anime")
                .releaseDate("06.06.2020")
                .genres(List.of("Action"))
                .build();
        when(animeService.updateAnime(eq("tt123123"), any(Anime.class))).thenReturn(updatedAnime);

        mockMvc.perform(put("/api/v1/anime/tt123123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedAnime)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(updatedAnime)));

        verify(animeService, times(1)).updateAnime(eq("tt123123"), any(Anime.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteAnime_shouldReturnNoContent() throws Exception {
        doNothing().when(animeService).deleteAnime("tt123123");

        mockMvc.perform(delete("/api/v1/anime/tt123123"))
                .andExpect(status().isNoContent());

        verify(animeService, times(1)).deleteAnime("tt123123");
    }

    @Test
    @WithMockUser
    void createAnime_shouldReturnForbidden_whenNotAdmin() throws Exception {
        mockMvc.perform(post("/api/v1/anime/create-anime")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(anime)))
                .andExpect(status().isForbidden());

        verify(animeService, never()).createNewAnime(any(Anime.class));
    }

    @Test
    @WithMockUser
    void updateAnime_shouldReturnForbidden_whenNotAdmin() throws Exception {
        mockMvc.perform(put("/api/v1/anime/tt123123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(anime)))
                .andExpect(status().isForbidden());

        verify(animeService, never()).updateAnime(anyString(), any(Anime.class));
    }

    @Test
    @WithMockUser
    void deleteAnime_shouldReturnForbidden_whenNotAdmin() throws Exception {
        mockMvc.perform(delete("/api/v1/anime/tt123123"))
                .andExpect(status().isForbidden());

        verify(animeService, never()).deleteAnime(anyString());
    }
}

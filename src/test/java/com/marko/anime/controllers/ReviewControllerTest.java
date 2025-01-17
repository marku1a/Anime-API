package com.marko.anime.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.marko.anime.configuration.TestSecurityConfig;
import com.marko.anime.dtos.ReviewStatus;
import com.marko.anime.dtos.ReviewSubmissionResult;
import com.marko.anime.models.Anime;
import com.marko.anime.models.Review;
import com.marko.anime.repositories.AnimeRepository;
import com.marko.anime.repositories.ReviewRepository;
import com.marko.anime.repositories.TokenRepository;
import com.marko.anime.services.JwtService;
import com.marko.anime.services.ReviewService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(ReviewController.class)
@Import(TestSecurityConfig.class)
class ReviewControllerTest {

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private TokenRepository tokenRepository;

    @MockBean
    private AnimeRepository animeRepository;

    @MockBean
    private ReviewRepository reviewRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    Review review;
    Anime anime;


    @BeforeEach
    void init() {
        ObjectId id = new ObjectId();
        review = Review.builder()
                .id(id)
                .body("testReview")
                .userId("testUser")
                .build();
        anime = Anime.builder()
                .id(new ObjectId())
                .imdbId("tt123123")
                .title("Test Anime")
                .releaseDate("05.05.2020")
                .trailer("yt.com")
                .poster("poster.img")
                .genres(List.of("Genre"))
                .backdrops(List.of("backdrop.img"))
                .reviewIds(List.of(review))
                .build();
    }
    @Test
    @WithMockUser
    void createReview_shouldReturnAccepted_whenApproved() throws Exception {
        Map<String, String> payload = Map.of(
                "reviewBody", review.getBody(),
                "imdbId", anime.getImdbId(),
                "userId", review.getUserId()
        );

        ReviewSubmissionResult result = new ReviewSubmissionResult(ReviewStatus.APPROVED, "Review published!");

        when(reviewService.submitReview(review.getBody(), anime.getImdbId(), review.getUserId())).thenReturn(result);

        mockMvc.perform(post("/api/v1/anime-reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isAccepted())
                .andExpect(content().string(result.getMessage()));

        verify(reviewService, times(1)).submitReview(review.getBody(), anime.getImdbId(), review.getUserId());
    }

    @Test
    @WithMockUser
    void createReview_shouldReturnBadRequest_whenRejected() throws Exception {
        Map<String, String> payload = Map.of(
                "reviewBody", review.getBody(),
                "imdbId", anime.getImdbId(),
                "userId", review.getUserId()
        );

        ReviewSubmissionResult result = new ReviewSubmissionResult(ReviewStatus.REJECTED, "Review rejected due to profanity!");

        when(reviewService.submitReview(review.getBody(), anime.getImdbId(), review.getUserId())).thenReturn(result);

        mockMvc.perform(post("/api/v1/anime-reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(result.getMessage()));

        verify(reviewService, times(1)).submitReview(review.getBody(), anime.getImdbId(), review.getUserId());
    }

    @Test
    @WithMockUser
    void createReview_shouldReturnInternalServerError_whenErrorOccurs() throws Exception {
        ReviewSubmissionResult errorResult = new ReviewSubmissionResult(ReviewStatus.ERROR, "Error processing review: Some error");
        when(reviewService.submitReview(anyString(), anyString(), anyString())).thenReturn(errorResult);

        mockMvc.perform(post("/api/v1/anime-reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"reviewBody\": \"Great anime!\", \"imdbId\": \"tt1234567\", \"userId\": \"user123\" }"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error processing review: Some error"));
    }
    @Test
    @WithMockUser
    void getAnimeReviews_shouldReturnStatusOk_whenSuccessful() throws Exception {
        List<Review> reviews = List.of(review);
        when(animeRepository.findAnimeByImdbId(anime.getImdbId())).thenReturn(Optional.of(anime));
        when(reviewRepository.getById(review.getId())).thenReturn(Optional.of(review));

        mockMvc.perform(get("/api/v1/anime-reviews/{imdbId}", anime.getImdbId()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(reviews)))
                .andDo(print());

        verify(animeRepository, times(1)).findAnimeByImdbId(anime.getImdbId());
        verify(reviewRepository, times(1)).getById(review.getId());
    }

    @Test
    @WithMockUser
    void getAnimeReviews_shouldReturnNotFound_whenAnimeNotFound() throws Exception {
        when(animeRepository.findAnimeByImdbId(anime.getImdbId())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/anime-reviews/{imdbId}", anime.getImdbId()))
                .andExpect(status().isNotFound());

        verify(animeRepository, times(1)).findAnimeByImdbId(anime.getImdbId());
    }

    @Test
    @WithMockUser
    void getAnimeReviews_shouldReturnInternalServerError_whenExceptionOccurs() throws Exception {
        when(animeRepository.findAnimeByImdbId(anime.getImdbId())).thenThrow(new DataAccessException("Database error") {
        });

        mockMvc.perform(get("/api/v1/anime-reviews/{imdbId}", anime.getImdbId()))
                .andExpect(status().isInternalServerError())
                .andDo(print());

        verify(animeRepository, times(1)).findAnimeByImdbId(anime.getImdbId());
        verify(reviewRepository, times(0)).getById(any(ObjectId.class));
    }



}


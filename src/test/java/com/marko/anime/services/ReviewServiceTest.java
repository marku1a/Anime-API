package com.marko.anime.services;

import com.marko.anime.dtos.ReviewStatus;
import com.marko.anime.dtos.ReviewSubmissionResult;
import com.marko.anime.models.Anime;
import com.marko.anime.models.Review;
import com.marko.anime.models.User;
import com.marko.anime.repositories.AnimeRepository;
import com.marko.anime.repositories.ReviewRepository;
import com.marko.anime.repositories.UserRepository;
import com.mongodb.client.result.UpdateResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.ExecutableUpdateOperation.ExecutableUpdate;
import org.springframework.data.mongodb.core.ExecutableUpdateOperation.TerminatingUpdate;
import org.springframework.data.mongodb.core.ExecutableUpdateOperation.UpdateWithUpdate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private AnimeRepository animeRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfanityFilterService profanityFilterService;

    @InjectMocks
    private ReviewService reviewService;

    private String body;
    private String imdbId;
    private String userId;
    private User user;
    private Review review;
    private Anime anime;

    @BeforeEach
    void init() {
        body = "This anime is awesome!";
        imdbId = "tt1234567";
        userId = "user123";
        user = new User();
        user.setUserId(userId);
        review = new Review(body, userId);
    }

    @Test
    void submitReview_ShouldReturnApproved_WhenSuccessful() {
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user));
        Anime anime = new Anime();
        anime.setReviewIds(new ArrayList<>());
        when(animeRepository.findAnimeByImdbId(imdbId)).thenReturn(Optional.of(anime));
        when(profanityFilterService.hasProfanity(body)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ExecutableUpdate executableUpdate = mock(ExecutableUpdate.class);
        TerminatingUpdate terminatingUpdate = mock(TerminatingUpdate.class);
        UpdateWithUpdate updateWithUpdate = mock(UpdateWithUpdate.class);

        when(mongoTemplate.update(Anime.class)).thenReturn(executableUpdate);
        when(executableUpdate.matching(any(Criteria.class))).thenReturn(updateWithUpdate);
        when(updateWithUpdate.apply(any(Update.class))).thenReturn(terminatingUpdate);
        when(terminatingUpdate.first()).thenReturn(UpdateResult.acknowledged(1, 1L, null));

        ReviewSubmissionResult result = reviewService.submitReview(body, imdbId, userId);

        assertThat(result.getStatus()).isEqualTo(ReviewStatus.APPROVED);
        assertThat(result.getMessage()).isEqualTo("Review published!");

        verify(userRepository, times(1)).findByUserId(userId);
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(mongoTemplate, times(1)).update(Anime.class);
        verify(executableUpdate, times(1)).matching(any(Criteria.class));
        verify(updateWithUpdate, times(1)).apply(any(Update.class));
        verify(terminatingUpdate, times(1)).first();
    }
    @Test
    void submitReview_ShouldReturnError_WhenUserNotFound() {
        when(userRepository.findByUserId(userId)).thenReturn(Optional.empty());

        ReviewSubmissionResult result = reviewService.submitReview(body, imdbId, userId);

        assertThat(result.getStatus()).isEqualTo(ReviewStatus.ERROR);
        assertThat(result.getMessage()).isEqualTo("Error processing review: User not found");
    }
    @Test
    void submitReview_ShouldReturnRejected_WhenReviewTooShort() {
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user));

        ReviewSubmissionResult result = reviewService.submitReview("Short review", imdbId, userId);

        assertThat(result.getStatus()).isEqualTo(ReviewStatus.REJECTED);
        assertThat(result.getMessage()).isEqualTo("Review must have at least 15 characters!");
    }
    @Test
    void submitReview_ShouldReturnRejected_WhenReviewAlreadyExists() {
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user));
        Anime anime = new Anime();
        anime.setReviewIds(List.of(review));
        when(animeRepository.findAnimeByImdbId(imdbId)).thenReturn(Optional.of(anime));
        when(reviewRepository.getById(any())).thenReturn(Optional.of(review));

        ReviewSubmissionResult result = reviewService.submitReview(body, imdbId, userId);

        assertThat(result.getStatus()).isEqualTo(ReviewStatus.REJECTED);
        assertThat(result.getMessage()).isEqualTo("Review for this anime has already been posted by user.");
    }
    @Test
    void submitReview_ShouldReturnRejected_WhenReviewHasProfanity() {
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user));
        when(profanityFilterService.hasProfanity(body)).thenReturn(true);

        ReviewSubmissionResult result = reviewService.submitReview(body, imdbId, userId);

        assertThat(result.getStatus()).isEqualTo(ReviewStatus.REJECTED);
        assertThat(result.getMessage()).isEqualTo("Review rejected due to profanity!");
    }
}
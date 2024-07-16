package com.marko.anime.services;

import com.marko.anime.models.Anime;
import com.marko.anime.models.Review;
import com.marko.anime.models.User;
import com.marko.anime.repositories.ReviewRepository;
import com.marko.anime.repositories.UserRepository;
import com.mongodb.client.result.UpdateResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.ExecutableUpdateOperation.ExecutableUpdate;
import org.springframework.data.mongodb.core.ExecutableUpdateOperation.TerminatingUpdate;
import org.springframework.data.mongodb.core.ExecutableUpdateOperation.UpdateWithUpdate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewService reviewService;

    private String body;
    private String imdbId;
    private String userId;
    private User user;
    private Review review;

    @BeforeEach
    void init() {
        body = "Awesome anime!";
        imdbId = "tt1234567";
        userId = "user123";
        user = new User();
        user.setUserId(userId);
        review = new Review(body, userId);
    }

    @Test
    void giveReview_ShouldReturnReview_WhenSuccessful() {
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user));
        when(reviewRepository.insert(any(Review.class))).thenReturn(review);

        ExecutableUpdate executableUpdate = mock(ExecutableUpdate.class);
        TerminatingUpdate terminatingUpdate = mock(TerminatingUpdate.class);
        UpdateWithUpdate updateWithUpdate = mock(UpdateWithUpdate.class);

        when(mongoTemplate.update(Anime.class)).thenReturn(executableUpdate);
        when(executableUpdate.matching(any(Criteria.class))).thenReturn(updateWithUpdate);
        when(updateWithUpdate.apply(any(Update.class))).thenReturn(terminatingUpdate);
        when(terminatingUpdate.first()).thenReturn(UpdateResult.acknowledged(1, 1L, null));


        Review createdReview = reviewService.giveReview(body, imdbId, userId);

        assertThat(createdReview).isNotNull();
        assertThat(createdReview.getBody()).isEqualTo(body);
        assertThat(createdReview.getUserId()).isEqualTo(userId);

        verify(userRepository, times(1)).findByUserId(userId);
        verify(reviewRepository, times(1)).insert(any(Review.class));
        verify(mongoTemplate, times(1)).update(Anime.class);
        verify(executableUpdate, times(1)).matching(any(Criteria.class));
        verify(updateWithUpdate, times(1)).apply(any(Update.class));
        verify(terminatingUpdate, times(1)).first();
    }

    @Test
    void giveReview_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.giveReview(body, imdbId, userId))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");

        verify(reviewRepository, never()).insert(any(Review.class));
        verify(mongoTemplate, never()).update(Anime.class);
    }

    @Test
    void giveReview_ShouldThrowException_WhenDatabaseErrorOccurs() {
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user));
        doThrow(new DataAccessException("Database error") {}).when(reviewRepository).insert(any(Review.class));

        assertThatThrownBy(() -> reviewService.giveReview(body, imdbId, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error occured while giving review.");

        verify(reviewRepository, times(1)).insert(any(Review.class));
        verify(mongoTemplate, never()).update(Anime.class);
    }

}
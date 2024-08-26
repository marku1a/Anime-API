package com.marko.anime.services;


import com.marko.anime.dtos.ReviewStatus;
import com.marko.anime.dtos.ReviewSubmissionResult;
import com.marko.anime.models.Anime;
import com.marko.anime.models.Review;
import com.marko.anime.models.User;
import com.marko.anime.repositories.AnimeRepository;
import com.marko.anime.repositories.ReviewRepository;
import com.marko.anime.repositories.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProfanityFilterService profanityFilterService;
    private final MongoTemplate mongoTemplate;
    private final AnimeRepository animeRepository;

    public ReviewService(ReviewRepository reviewRepository, UserRepository userRepository, ProfanityFilterService profanityFilterService, MongoTemplate mongoTemplate,
                         AnimeRepository animeRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.profanityFilterService = profanityFilterService;
        this.mongoTemplate = mongoTemplate;
        this.animeRepository = animeRepository;
    }

    public ReviewSubmissionResult submitReview(String body, String imdbId, String userId) {
        try {
            User user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found."));
            if (body.length() < 50) {
                return new ReviewSubmissionResult(ReviewStatus.REJECTED,
                        "Review must have at least 50 characters!");
            }
            if (reviewByUserExists(imdbId, userId)) {
                return new ReviewSubmissionResult(ReviewStatus.REJECTED,
                        "Review for this anime has already been posted by user.");
            }
            if (profanityFilterService.hasProfanity(body)) {
                return new ReviewSubmissionResult(ReviewStatus.REJECTED,
                        "Review rejected due to profanity!");
            }
            return saveReviewAndUpdateAnime(body, imdbId, userId);
        } catch (Exception e) {
            return new ReviewSubmissionResult(ReviewStatus.ERROR, "Error processing review: " + e.getMessage());
        }
    }


    boolean reviewByUserExists(String imdbId, String userId) {
        return animeRepository.findAnimeByImdbId(imdbId)
                .map(anime -> anime.getReviewIds().stream()
                        .map(id -> reviewRepository.getById(id.getId()).orElse(null))
                        .filter(Objects::nonNull)
                        .anyMatch(review -> review.getUserId().equals(userId)))
                .orElse(false);
    }
    private void updateAnimeWithReviewId(String imdbId, ObjectId reviewId){
        mongoTemplate.update(Anime.class)
                .matching(Criteria.where("imdbId").is(imdbId))
                .apply(new Update().push("ReviewIds").value(reviewId))
                .first();
    }

    public ReviewSubmissionResult saveReviewAndUpdateAnime(String body, String imdbId, String userId) {
            Review review = new Review(body, userId);
            Review savedReview = reviewRepository.save(review);
            updateAnimeWithReviewId(imdbId, savedReview.getId());
            return new ReviewSubmissionResult(ReviewStatus.APPROVED,
                    "Review published!");
    }
}



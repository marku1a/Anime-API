package com.marko.anime.services;


import com.marko.anime.dtos.ReviewSubmissionResult;
import com.marko.anime.models.Review;
import com.marko.anime.models.User;
import com.marko.anime.repositories.ReviewRepository;
import com.marko.anime.repositories.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ReviewPublishService reviewPublishService;

    public ReviewService(ReviewRepository reviewRepository, UserRepository userRepository, ReviewPublishService reviewPublishService) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.reviewPublishService = reviewPublishService;
    }

    public ReviewSubmissionResult submitReview(String body, String imdbId, String userId) throws Exception {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (body.length() < 15) {
            throw new IllegalArgumentException("Review must have at least 15 characters!");
        }
        Review review = new Review(body, userId);
        Review savedReview = reviewRepository.save(review);

        return reviewPublishService.publishAndWaitForResult(savedReview, imdbId);
    }
}

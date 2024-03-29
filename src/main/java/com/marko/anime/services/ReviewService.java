package com.marko.anime.services;


import com.marko.anime.models.Anime;
import com.marko.anime.models.Review;
import com.marko.anime.models.User;
import com.marko.anime.repositories.ReviewRepository;
import com.marko.anime.repositories.UserRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MongoTemplate mongoTemplate;
    private final UserRepository userRepository;
    public ReviewService(ReviewRepository reviewRepository, MongoTemplate mongoTemplate, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.mongoTemplate = mongoTemplate;
        this.userRepository = userRepository;
    }

    public Review giveReview(String body, String imdbId, String userId) throws Exception {
        Review review = new Review(body, userId);
        Optional<User> userOpt = userRepository.findByUserId(userId);
        if (userOpt.isEmpty()) {
            throw new Exception("User does not exist.");
        }
        reviewRepository.insert(review);
        mongoTemplate.update(Anime.class).matching(Criteria.where("imdbId").is(imdbId))
                .apply(new Update().push("reviewIds").value(review.getId()))
                .first();
        return review;
    }
}

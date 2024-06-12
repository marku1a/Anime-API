package com.marko.anime.services;


import com.marko.anime.models.Anime;
import com.marko.anime.models.Review;
import com.marko.anime.models.User;
import com.marko.anime.repositories.ReviewRepository;
import com.marko.anime.repositories.UserRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    public Review giveReview(String body, String imdbId, String userId) throws UsernameNotFoundException, DataAccessException {
        try {
            Review review = new Review(body, userId);
            Optional<User> userOpt = Optional.ofNullable(userRepository.findByUserId(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found")));
            reviewRepository.insert(review);
            mongoTemplate.update(Anime.class).matching(Criteria.where("imdbId").is(imdbId))
                    .apply(new Update().push("reviewIds").value(review.getId()))
                    .first();
            return review;
        } catch (DataAccessException e) {
            throw new RuntimeException("Database error occured while giving review.", e);
        }
    }
}

package com.marko.anime.services;

import com.marko.anime.dtos.ReviewStatus;
import com.marko.anime.dtos.ReviewSubmissionResult;
import com.marko.anime.models.Anime;
import com.marko.anime.models.Review;
import com.marko.anime.repositories.ReviewRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class ReviewCheckService {
    private final ProfanityFilterService profanityFilterService;
    private final ReviewRepository reviewRepository;
    private final MongoTemplate mongoTemplate;

    public ReviewCheckService(ProfanityFilterService profanityFilterService, ReviewRepository reviewRepository, MongoTemplate mongoTemplate) {
        this.profanityFilterService = profanityFilterService;
        this.reviewRepository = reviewRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @RabbitListener(queues = "reviewCheckQueue")
    public ReviewSubmissionResult checkReview(@Payload Review review, @Header("imdbId") String imdbId) {
        try {
            boolean isProfane = profanityFilterService.hasProfanity(review.getBody());

            if (!isProfane) {
                mongoTemplate.update(Anime.class)
                        .matching(Criteria.where("imdbId").is(imdbId))
                        .apply(new Update().push("reviewIds").value(review))
                        .first();

                return new ReviewSubmissionResult(review.getId(), ReviewStatus.APPROVED, "Review published!");
            } else {
                reviewRepository.delete(review);
                return new ReviewSubmissionResult(review.getId(), ReviewStatus.REJECTED, "Review rejected due to profanity.");
            }
        } catch (Exception e) {
            return new ReviewSubmissionResult(review.getId(), ReviewStatus.ERROR, "Error processing review:" + e.getMessage());
        }
    }
}
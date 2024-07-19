package com.marko.anime.services;

import com.marko.anime.dtos.ReviewSubmissionResult;
import com.marko.anime.models.Review;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

@Service
public class ReviewPublishService {

    private final RabbitTemplate rabbitTemplate;

    public ReviewPublishService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public ReviewSubmissionResult publishAndWaitForResult(Review review, String imdbId) {
        return rabbitTemplate.convertSendAndReceiveAsType(
                "reviewExchange",
                "review.check",
                review,
                message -> {
                    message.getMessageProperties().setHeader("imdbId", imdbId);
                    return message;
                },
                new ParameterizedTypeReference<ReviewSubmissionResult>() {}
        );
    }
}

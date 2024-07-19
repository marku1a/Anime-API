package com.marko.anime.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

@Data
@Builder
@AllArgsConstructor
public class ReviewSubmissionResult {
    private ObjectId reviewId;
    private ReviewStatus status;
    private String message;
}

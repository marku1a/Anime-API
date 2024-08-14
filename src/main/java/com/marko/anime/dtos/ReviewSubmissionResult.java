package com.marko.anime.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ReviewSubmissionResult {
    private ReviewStatus status;
    private String message;
}

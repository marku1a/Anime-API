package com.marko.anime.controllers;

import com.marko.anime.models.Review;
import com.marko.anime.services.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/v1/anime-reviews")
public class ReviewController {

    private final ReviewService reviewService;
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }
    @PostMapping
    public ResponseEntity<Review> createReview(@RequestBody Map<String,String> payload) throws Exception {
        return new ResponseEntity<Review>(reviewService
                .giveReview(payload.get("reviewBody"), payload.get("imdbId"), payload.get("userId")), HttpStatus.CREATED);
    }
}

package com.marko.anime.controllers;

import com.marko.anime.dtos.ReviewStatus;
import com.marko.anime.dtos.ReviewSubmissionResult;
import com.marko.anime.models.Anime;
import com.marko.anime.models.Review;
import com.marko.anime.repositories.AnimeRepository;
import com.marko.anime.repositories.ReviewRepository;
import com.marko.anime.services.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/anime-reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final AnimeRepository animeRepository;
    private final ReviewRepository reviewRepository;

    public ReviewController(ReviewService reviewService,
                            AnimeRepository animeRepository,
                            ReviewRepository reviewRepository) {
        this.reviewService = reviewService;
        this.animeRepository = animeRepository;
        this.reviewRepository = reviewRepository;
    }
    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody Map<String, String> payload) {
        try {
            ReviewSubmissionResult result = reviewService.submitReview(payload.get("reviewBody"), payload.get("imdbId"), payload.get("userId"));
            if ((result.getStatus()).equals(ReviewStatus.APPROVED)) {
                return new ResponseEntity<>("Review submitted and approved", HttpStatus.ACCEPTED);
            } else {
                return new ResponseEntity<>(result.getMessage(), HttpStatus.BAD_REQUEST);
            }
        } catch (UsernameNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
   @GetMapping("/{imdbId}")
    public ResponseEntity<List<Review>> getAnimeReviews(@PathVariable String imdbId) {
        try {
            Optional<Anime> animeOpt = animeRepository.findAnimeByImdbId(imdbId);
            if(animeOpt.isPresent()) {
                List<Review> reviews = animeOpt.get().getReviewIds().stream()
                        .map(id -> reviewRepository.getById(id.getId()).orElse(null))
                       .filter(Objects::nonNull)
                       .toList();
                return new ResponseEntity<>(reviews, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
   }
}

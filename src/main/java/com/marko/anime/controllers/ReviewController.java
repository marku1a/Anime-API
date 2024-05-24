package com.marko.anime.controllers;

import com.marko.anime.models.Anime;
import com.marko.anime.models.Review;
import com.marko.anime.repositories.AnimeRepository;
import com.marko.anime.repositories.ReviewRepository;
import com.marko.anime.services.ReviewService;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
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
    public ResponseEntity<Review> createReview(@RequestBody Map<String,String> payload) throws Exception {
        return new ResponseEntity<Review>(reviewService
                .giveReview(payload.get("reviewBody"), payload.get("imdbId"), payload.get("userId")), HttpStatus.CREATED);
    }
   @GetMapping("/{imdbId}")
    public ResponseEntity<List<Review>> getAnimeReviews(@PathVariable String imdbId) {
        try {
            Optional<Anime> animeOpt = animeRepository.findAnimeByImdbId(imdbId);
            if(animeOpt.isPresent()) {
                List<ObjectId> reviewIds = animeOpt.get().getReviewIds().stream()
                        .map(Review::getId)
                        .toList();
                List<Review> reviews = reviewIds.stream()
                        .flatMap(id -> reviewRepository.getById(id).stream())
                        .toList();
                return new ResponseEntity<>(reviews, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
   }
}

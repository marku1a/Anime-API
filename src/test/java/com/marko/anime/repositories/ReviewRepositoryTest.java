package com.marko.anime.repositories;


import com.marko.anime.models.Review;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@TestPropertySource(locations = "classpath:application-test.properties")
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    private Review review1;
    private Review review2;

    @BeforeEach
    void init() {

        review1 = Review.builder()
                .body("This is the first review")
                .userId("user123")
                .build();

        review2 = Review.builder()
                .body("This is the second review")
                .userId("user456")
                .build();

        reviewRepository.saveAll(List.of(review1, review2));
    }

    @AfterEach
    void tearDown() {
        reviewRepository.deleteAll();
    }

    @Test
    void save_shouldReturnSavedReview() {
        Review review = Review.builder()
                .body("This is a review")
                .userId("user123")
                .build();

        Review saved = reviewRepository.save(review);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
    }


    @Test
    void getById_shouldReturnCorrectReview() {
        Optional<Review> foundReview = reviewRepository.getById(review1.getId());

        assertThat(foundReview).isPresent().contains(review1);
    }

    @Test
    void getById_shouldReturnEmptyForNonExistentId() {
        Optional<Review> foundReview = reviewRepository.getById(new ObjectId());

        assertThat(foundReview).isEmpty();
    }

    @Test
    void delete_shouldReturnEmptyWhenDeleted() {
        reviewRepository.delete(review1);

        Optional<Review> foundReview = reviewRepository.getById(review1.getId());

        assertThat(foundReview).isEmpty();
    }

    @Test
    void findAll_shouldReturnAllReviews() {
        List<Review> reviews = reviewRepository.findAll();

        assertThat(reviews).isNotNull().hasSize(2).containsExactlyInAnyOrder(review1, review2);
    }
}
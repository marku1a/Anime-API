package com.marko.anime.repositories;

import com.marko.anime.models.Review;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository

public interface ReviewRepository extends MongoRepository<Review, ObjectId> {
}

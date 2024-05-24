package com.marko.anime.repositories;

import com.marko.anime.models.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, ObjectId> {

    Optional<User> findByEmail(String email);
    Optional<User> findByUserId(String userId);

    @Query("{ 'userId' : { $regex: ?0, $options: 'i' } }")
    Optional<User> findByUserIdIgnoreCase(String userId);

}

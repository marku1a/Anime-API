package com.marko.anime.repositories;

import com.marko.anime.models.Token;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends MongoRepository<Token, ObjectId> {

    @Query(value = "{ 'user.id' : ?0, $or: [{ 'expired': ?1 }, { 'revoked': ?2 }] }")
    List<Token> findAllValidTokenByUser(ObjectId userId, boolean expired, boolean revoked);

    Optional<Token> findByToken(String token);
}

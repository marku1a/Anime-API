package com.marko.anime.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "reviews")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Review {
    private ObjectId id;
    private String body;
    private String userId; //"username"
    private LocalDateTime createdAt;

    public Review(String body, String userId) {
        this.body = body;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
    }


}

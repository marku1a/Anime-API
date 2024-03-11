package com.marko.anime.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection="tokens")
public class Token {

    @Id
    public ObjectId id;
    @Indexed(unique = true)
    public String token;
    public String tokenType;
    public boolean revoked;
    public boolean expired;
    @DBRef
    public User user;
}
package com.marko.anime.repositories;

import com.marko.anime.models.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@TestPropertySource(locations = "classpath:application-test.properties")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    private User user;

    @BeforeEach
    void init() {
        user = User.builder()
            .firstName("John")
            .lastName("Wick")
            .email("john.wick@mail.com")
            .userId("johnwick")
            .password("password")
            .role("USER")
            .accountNonLocked(true)
            .build();

        userRepository.save(user);
    }

    @Test
    void save_shouldReturnSavedUser() {

        User savedUser = userRepository.save(user);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
    }

    @Test
    void findByEmail_shouldReturnUser() {
        Optional<User> foundUser = userRepository.findByEmail("john.wick@mail.com");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("john.wick@mail.com");
    }

    @Test
    void findByUserId_shouldReturnUser() {
        Optional<User> foundUser = userRepository.findByUserId("johnwick");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUserId()).isEqualTo("johnwick");
    }

    @Test
    void findByUserIdIgnoreCase_shouldReturnUser() {
        Optional<User> foundUser = userRepository.findByUserIdIgnoreCase("jOhNwIcK");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUserId()).isEqualTo("johnwick");
    }
}
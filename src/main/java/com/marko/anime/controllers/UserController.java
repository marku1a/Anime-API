package com.marko.anime.controllers;

import com.marko.anime.dtos.UserInfoDTO;
import com.marko.anime.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/users")
public class UserController {

    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserInfoDTO>> getUserInfo() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/{userId}/ban")
    public ResponseEntity<Void> banUser(@PathVariable String userId) {
        userService.banUser(userId);
        return ResponseEntity.noContent().build();
    }

}

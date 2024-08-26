package com.marko.anime.controllers;

import com.marko.anime.dtos.AuthenticationRequest;
import com.marko.anime.dtos.AuthenticationResponse;
import com.marko.anime.dtos.RegisterRequest;
import com.marko.anime.services.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authService;


    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequest request,
            HttpServletResponse response) throws IllegalArgumentException {

            AuthenticationResponse authResponse = authService.register(request);
            return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(
            @RequestBody AuthenticationRequest request,
            HttpServletResponse response) throws BadCredentialsException, LockedException {

            AuthenticationResponse authResponse = authService.authenticate(request);
            authService.setRTokenAsCookie(response, authResponse.getRefreshToken());
            return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/refresh-token")
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        authService.refreshToken(request, response);
    }


}

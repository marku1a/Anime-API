package com.marko.anime.controllers;


import com.marko.anime.models.Anime;
import com.marko.anime.services.AnimeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/anime")
public class AnimeController {

    private final AnimeService animeService;

    public AnimeController(AnimeService animeService) {
        this.animeService = animeService;
    }

    @GetMapping
    public ResponseEntity<List<Anime>> getAllAnime() {
        return ResponseEntity.ok(animeService.findAllAnime());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Anime>> getAnimeById(@PathVariable String id) {
        return ResponseEntity.ok(animeService.findAnimeByImdbId(id));
    }

    @PostMapping("/create-anime")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Anime> createAnime(@RequestBody Anime anime) {
        return ResponseEntity.status(HttpStatus.CREATED).body(animeService.createNewAnime(anime));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Anime> updateAnime(@PathVariable String id, @RequestBody Anime anime) {
        Anime updatedAnime = animeService.updateAnime(id, anime);
        return ResponseEntity.ok(updatedAnime);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAnime(@PathVariable String id) {
        animeService.deleteAnime(id);
        return ResponseEntity.noContent().build();
    }
}

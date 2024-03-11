package com.marko.anime.controllers;


import com.marko.anime.services.AnimeService;
import com.marko.anime.models.Anime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:3000", methods = {RequestMethod.GET, RequestMethod.POST})
@RequestMapping("/api/v1/anime")
public class AnimeController {

    private final AnimeService animeService;
    public AnimeController(AnimeService animeService){
        this.animeService = animeService;
    }
    @GetMapping
    public ResponseEntity<List<Anime>> getAllAnime() {
        return new ResponseEntity<List<Anime>>(animeService.findAllAnime(), HttpStatus.OK);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Optional<Anime>> getAnimeById(@PathVariable String id) {
        return new ResponseEntity<Optional<Anime>>(animeService.findAnimeByImdbId(id), HttpStatus.OK);
    }

}

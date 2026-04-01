package com.urlshortener.urlservice.controller;

import com.urlshortener.urlservice.dto.ShortenRequest;
import com.urlshortener.urlservice.dto.ShortenResponse;
import com.urlshortener.urlservice.service.UrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @PostMapping("/api/shorten")
    public ResponseEntity<ShortenResponse> shorten(
            @RequestBody ShortenRequest request){
        return ResponseEntity.ok(urlService.shorten(request));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<Void> redirect(@PathVariable String slug){
        String originalUrl = urlService.resolve(slug);
        return ResponseEntity
                .status(302)
                .location(URI.create(originalUrl))
                .build();
    }

}

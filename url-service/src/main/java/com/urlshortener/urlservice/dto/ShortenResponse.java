package com.urlshortener.urlservice.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class ShortenResponse {
    private String slug;
    private String shortUrl;
    private String originalUrl;
}
package com.urlshortener.urlservice.dto;

import lombok.Data;

@Data
public class ShortenRequest {
    private String originalUrl;
    private Integer expiryDays; // optional
}
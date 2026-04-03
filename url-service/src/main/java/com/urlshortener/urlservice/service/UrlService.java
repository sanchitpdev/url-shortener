package com.urlshortener.urlservice.service;

import com.urlshortener.urlservice.dto.ShortenRequest;
import com.urlshortener.urlservice.dto.ShortenResponse;
import com.urlshortener.urlservice.model.UrlMapping;
import com.urlshortener.urlservice.repository.UrlMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlService {

    private final UrlMappingRepository urlMappingRepository;
    private final StringRedisTemplate redisTemplate;

    private static final String BASE_URL =
            "http://urlshortener-alb-1372942651.ap-south-1.elb.amazonaws.com/";
    private static final String REDIS_PREFIX = "slug:";

    public ShortenResponse shorten(ShortenRequest request){
        String slug = generateSlug();

        UrlMapping mapping = new UrlMapping();
        mapping.setSlug(slug);
        mapping.setOriginalUrl(request.getOriginalUrl());

        if (request.getExpiryDays() != null){
            mapping.setExpiresAt(LocalDateTime.now()
                    .plusDays(request.getExpiryDays()));
        }

        urlMappingRepository.save(mapping);

        //Cache in redis for 24 hours
        redisTemplate.opsForValue().set(
                REDIS_PREFIX + slug,
                request.getOriginalUrl(),
                24, TimeUnit.HOURS
        );

        log.info("Created short URL: {}{}",BASE_URL,slug);

        return new ShortenResponse(slug,BASE_URL+slug,
                request.getOriginalUrl());
    }

    public String resolve(String slug){
        //Check Redis cache first
        String cached = redisTemplate.opsForValue()
                .get(REDIS_PREFIX + slug);

        if (cached != null){
            log.info("Cache hit for slug: {}", slug);
            return cached;
        }

        //Fall back to postgres
        log.info("Cache miss for slug: {}, hitting DB", slug);
        return urlMappingRepository.findBySlug(slug)
                .map(UrlMapping::getOriginalUrl)
                .orElseThrow(() -> new RuntimeException("Slug not found: "+ slug));

    }

    private String generateSlug() {
        return java.util.UUID.randomUUID()
                .toString()
                .replace("-","")
                .substring(0,7);
    }
}

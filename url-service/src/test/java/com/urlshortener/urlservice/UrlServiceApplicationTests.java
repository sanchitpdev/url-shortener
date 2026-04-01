package com.urlshortener.urlservice;

import com.urlshortener.urlservice.dto.ShortenRequest;
import com.urlshortener.urlservice.dto.ShortenResponse;
import com.urlshortener.urlservice.model.UrlMapping;
import com.urlshortener.urlservice.repository.UrlMappingRepository;
import com.urlshortener.urlservice.service.UrlService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Nested
@ExtendWith(MockitoExtension.class)
class UrlServiceApplicationTests {

	@Mock
	private UrlMappingRepository urlMappingRepository;

	@Mock
	private StringRedisTemplate redisTemplate;

	@Mock
	private ValueOperations<String, String> valueOperations;

	@InjectMocks
	private UrlService urlService;

	@Test
	void shorten_shouldReturnValidSlugAndShortUrl() {
		// Arrange
		ShortenRequest request = new ShortenRequest();
		request.setOriginalUrl("https://github.com");
		request.setExpiryDays(7);

		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(urlMappingRepository.save(any(UrlMapping.class)))
				.thenAnswer(i -> i.getArgument(0));

		// Act
		ShortenResponse response = urlService.shorten(request);

		// Assert
		assertNotNull(response.getSlug());
		assertEquals(7, response.getSlug().length());
		assertTrue(response.getShortUrl().contains(response.getSlug()));
		assertEquals("https://github.com", response.getOriginalUrl());
	}

	@Test
	void resolve_shouldReturnCachedUrlWhenRedisHit() {
		// Arrange
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get("slug:abc1234"))
				.thenReturn("https://github.com");

		// Act
		String result = urlService.resolve("abc1234");

		// Assert
		assertEquals("https://github.com", result);
		// Verify Postgres was never touched
		verify(urlMappingRepository, never()).findBySlug(any());
	}

	@Test
	void resolve_shouldFallbackToPostgresWhenCacheMiss() {
		// Arrange
		UrlMapping mapping = new UrlMapping();
		mapping.setSlug("abc1234");
		mapping.setOriginalUrl("https://github.com");

		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get("slug:abc1234")).thenReturn(null);
		when(urlMappingRepository.findBySlug("abc1234"))
				.thenReturn(Optional.of(mapping));

		// Act
		String result = urlService.resolve("abc1234");

		// Assert
		assertEquals("https://github.com", result);
		// Verify Postgres WAS called
		verify(urlMappingRepository, times(1)).findBySlug("abc1234");
	}
}

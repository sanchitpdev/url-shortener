package com.urlshortener.urlservice.repository;

import com.urlshortener.urlservice.model.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {
    Optional<UrlMapping> findBySlug(String slug);
}
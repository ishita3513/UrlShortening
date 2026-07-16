package com.app.service;

import com.app.dto.CreateUrlRequest;
import com.app.dto.CreateUrlResponse;
import com.app.entity.UrlEntity;
import com.app.entity.UrlStatus;
import com.app.exception.ConflictException;
import com.app.exception.NotFoundException;
import com.app.exception.UrlExpiredException;
import com.app.repository.UrlRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
public class UrlService {

    private final UrlRepository urlRepository;
    private final UrlValidationService urlValidationService;
    private final RateLimitService rateLimitService;
    private final UrlCacheService urlCacheService;
    private final ShortCodeGenerator shortCodeGenerator;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public UrlService(
            UrlRepository urlRepository,
            UrlValidationService urlValidationService,
            RateLimitService rateLimitService,
            UrlCacheService urlCacheService,
            ShortCodeGenerator shortCodeGenerator) {
        this.urlRepository = urlRepository;
        this.urlValidationService = urlValidationService;
        this.rateLimitService = rateLimitService;
        this.urlCacheService = urlCacheService;
        this.shortCodeGenerator = shortCodeGenerator;
    }

    public CreateUrlResponse createUrl(CreateUrlRequest request, String clientIp) {
        urlValidationService.validateOriginalUrl(request.getOriginalUrl());
        if (StringUtils.hasText(request.getCustomAlias())) {
            urlValidationService.validateCustomAlias(request.getCustomAlias());
            if (urlRepository.existsByCustomAlias(request.getCustomAlias())) {
                throw new ConflictException("Alias is already taken");
            }
        }

        rateLimitService.checkOrThrow(clientIp);

        Instant now = Instant.now();
        int expiryDays = request.getExpiredInDays() == null ? 30 : request.getExpiredInDays();
        Instant expiresAt = now.plus(Duration.ofDays(expiryDays));

        UrlEntity entity = new UrlEntity();
        entity.setOriginalUrl(request.getOriginalUrl());
        entity.setCustomAlias(StringUtils.hasText(request.getCustomAlias()) ? request.getCustomAlias() : null);
        entity.setCreatedAt(now);
        entity.setExpiresAt(expiresAt);
        entity.setStatus(UrlStatus.ACTIVE);
        entity.setShortCode("TEMP");

        UrlEntity saved = urlRepository.save(entity);
        String shortCode = shortCodeGenerator.generate(saved.getId());
        saved.setShortCode(shortCode);
        saved = urlRepository.save(saved);
        urlCacheService.put(saved);

        return new CreateUrlResponse(
                saved.getShortCode(),
                baseUrl + "/" + saved.getShortCode(),
                saved.getOriginalUrl(),
                saved.getExpiresAt()
        );
    }

    public String redirect(String token) {
        Optional<UrlEntity> cachedByShortCode = urlCacheService.getByShortCode(token);
        Optional<UrlEntity> cachedByAlias = urlCacheService.getByAlias(token);

        if (cachedByShortCode.isPresent()) {
            return validateAndGetRedirectUrl(cachedByShortCode.get());
        }
        if (cachedByAlias.isPresent()) {
            return validateAndGetRedirectUrl(cachedByAlias.get());
        }

        Optional<UrlEntity> persisted = urlRepository.findByShortCode(token);
        if (persisted.isEmpty()) {
            persisted = urlRepository.findByCustomAlias(token);
        }
        if (persisted.isEmpty()) {
            throw new NotFoundException("Short URL not found");
        }

        UrlEntity entity = persisted.get();
        urlCacheService.put(entity);
        return validateAndGetRedirectUrl(entity);
    }

    private String validateAndGetRedirectUrl(UrlEntity entity) {
        if (entity.getStatus() == UrlStatus.EXPIRED || entity.isExpired()) {
            entity.setStatus(UrlStatus.EXPIRED);
            urlRepository.save(entity);
            throw new UrlExpiredException("Short URL has expired");
        }
        if (entity.getStatus() == UrlStatus.DELETED) {
            throw new NotFoundException("Short URL not found");
        }
        entity.setClickCount(entity.getClickCount() + 1);
        urlRepository.save(entity);
        return entity.getOriginalUrl();
    }
}

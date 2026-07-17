package com.app.service;

import com.app.entity.UrlEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class RedisUrlCacheService implements UrlCacheService {

    private static final Logger log = LoggerFactory.getLogger(RedisUrlCacheService.class);
    private static final String SHORT_CODE_PREFIX = "urlcache:short:";
    private static final String ALIAS_PREFIX = "urlcache:alias:";
    private static final Duration TTL = Duration.ofHours(6);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisUrlCacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<UrlEntity> getByShortCode(String shortCode) {
        return get(SHORT_CODE_PREFIX + shortCode);
    }

    @Override
    public Optional<UrlEntity> getByAlias(String alias) {
        return get(ALIAS_PREFIX + alias);
    }

    @Override
    public void put(UrlEntity urlEntity) {
        String json;
        try {
            json = objectMapper.writeValueAsString(urlEntity);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize UrlEntity for caching, skipping cache write", e);
            return;
        }
        if (urlEntity.getShortCode() != null) {
            redisTemplate.opsForValue().set(SHORT_CODE_PREFIX + urlEntity.getShortCode(), json, TTL);
        }
        if (urlEntity.getCustomAlias() != null) {
            redisTemplate.opsForValue().set(ALIAS_PREFIX + urlEntity.getCustomAlias(), json, TTL);
        }
    }

    private Optional<UrlEntity> get(String key) {
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, UrlEntity.class));
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize cached UrlEntity for key {}, treating as cache miss", key, e);
            return Optional.empty();
        }
    }
}

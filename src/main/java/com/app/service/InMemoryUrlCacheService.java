package com.app.service;

import com.app.entity.UrlEntity;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryUrlCacheService implements UrlCacheService {

    private final Map<String, UrlEntity> cache = new ConcurrentHashMap<>();

    @Override
    public Optional<UrlEntity> getByShortCode(String shortCode) {
        return Optional.ofNullable(cache.get("short:" + shortCode));
    }

    @Override
    public Optional<UrlEntity> getByAlias(String alias) {
        return Optional.ofNullable(cache.get("alias:" + alias));
    }

    @Override
    public void put(UrlEntity urlEntity) {
        if (urlEntity.getShortCode() != null) {
            cache.put("short:" + urlEntity.getShortCode(), urlEntity);
        }
        if (urlEntity.getCustomAlias() != null) {
            cache.put("alias:" + urlEntity.getCustomAlias(), urlEntity);
        }
    }
}

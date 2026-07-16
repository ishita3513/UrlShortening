package com.app.service;

import com.app.entity.UrlEntity;

import java.util.Optional;

public interface UrlCacheService {
    Optional<UrlEntity> getByShortCode(String shortCode);
    Optional<UrlEntity> getByAlias(String alias);
    void put(UrlEntity urlEntity);
}

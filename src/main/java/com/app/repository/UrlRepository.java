package com.app.repository;

import com.app.entity.UrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<UrlEntity, Long> {
    Optional<UrlEntity> findByShortCode(String shortCode);
    Optional<UrlEntity> findByCustomAlias(String customAlias);
    boolean existsByCustomAlias(String customAlias);
}

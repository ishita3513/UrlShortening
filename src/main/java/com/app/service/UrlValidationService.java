package com.app.service;

import com.app.exception.BadRequestException;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;

@Service
public class UrlValidationService {

    public void validateOriginalUrl(String originalUrl) {
        if (originalUrl == null || originalUrl.isBlank()) {
            throw new BadRequestException("originalUrl is required");
        }

        try {
            URI uri = new URI(originalUrl);
            if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) {
                throw new BadRequestException("originalUrl must use http or https");
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new BadRequestException("originalUrl must include a valid host");
            }
        } catch (URISyntaxException ex) {
            throw new BadRequestException("originalUrl is invalid");
        }
    }

    public void validateCustomAlias(String customAlias) {
        if (customAlias == null || customAlias.isBlank()) {
            return;
        }

        if (!customAlias.matches("^[A-Za-z0-9-]{2,50}$")) {
            throw new BadRequestException("customAlias must be 2-50 characters and contain only letters, numbers and hyphens");
        }
    }
}

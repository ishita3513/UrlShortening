package com.app.service;

public interface RateLimitService {
    void checkOrThrow(String clientIp);
}

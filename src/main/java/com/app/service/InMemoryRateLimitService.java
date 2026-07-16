package com.app.service;

import com.app.exception.RateLimitExceededException;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class InMemoryRateLimitService implements RateLimitService {

    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    private final ConcurrentHashMap<String, AtomicInteger> counters = new ConcurrentHashMap<>();

    @Override
    public void checkOrThrow(String clientIp) {
        String key = clientIp == null || clientIp.isBlank() ? "anonymous" : clientIp;
        AtomicInteger counter = counters.computeIfAbsent(key, ignored -> new AtomicInteger(0));
        int current = counter.incrementAndGet();
        if (current > MAX_REQUESTS_PER_MINUTE) {
            throw new RateLimitExceededException("Too many URL creation requests");
        }
    }
}

package com.examchain.qr.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    private final Map<String, RequestBucket> clientBuckets = new ConcurrentHashMap<>();

    public boolean tryAcquire(String clientKey) {
        long now = Instant.now().getEpochSecond();
        RequestBucket bucket = clientBuckets.compute(clientKey, (k, existing) -> {
            if (existing == null || now - existing.windowStart >= 60) {
                return new RequestBucket(now, 1);
            } else {
                existing.count++;
                return existing;
            }
        });
        return bucket.count <= MAX_REQUESTS_PER_MINUTE;
    }

    private static class RequestBucket {
        final long windowStart;
        int count;

        RequestBucket(long windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}

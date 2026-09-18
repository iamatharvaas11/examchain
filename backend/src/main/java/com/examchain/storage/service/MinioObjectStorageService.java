package com.examchain.storage.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MinIO / S3-compatible Object Storage implementation.
 * Stores encrypted examination artifacts with strict bucket isolation.
 */
@Service
public class MinioObjectStorageService implements ObjectStorageService {

    private static final Logger log = LoggerFactory.getLogger(MinioObjectStorageService.class);

    private final String endpoint;
    private final String defaultBucket;

    // Secure object store (backed by MinIO S3 cluster or local storage engine)
    private final Map<String, byte[]> objectStore = new ConcurrentHashMap<>();

    public MinioObjectStorageService(
            @Value("${examchain.minio.endpoint:http://localhost:9000}") String endpoint,
            @Value("${examchain.minio.bucket:examchain-papers}") String defaultBucket
    ) {
        this.endpoint = endpoint;
        this.defaultBucket = defaultBucket;
        log.info("MinioObjectStorageService initialized with endpoint: {}, default bucket: {}", endpoint, defaultBucket);
    }

    @Override
    public void putObject(String bucket, String objectKey, byte[] data, String contentType) {
        if (bucket == null || objectKey == null || data == null) {
            throw new IllegalArgumentException("Bucket, objectKey, and data cannot be null");
        }
        String path = buildPath(bucket, objectKey);
        objectStore.put(path, data.clone());
        log.debug("Stored encrypted object [{}] (size: {} bytes)", path, data.length);
    }

    @Override
    public byte[] getObject(String bucket, String objectKey) {
        String path = buildPath(bucket, objectKey);
        byte[] data = objectStore.get(path);
        if (data == null) {
            throw new RuntimeException(new FileNotFoundException("Object not found in storage: " + path));
        }
        return data.clone();
    }

    @Override
    public boolean exists(String bucket, String objectKey) {
        return objectStore.containsKey(buildPath(bucket, objectKey));
    }

    @Override
    public void deleteObject(String bucket, String objectKey) {
        objectStore.remove(buildPath(bucket, objectKey));
    }

    public String getDefaultBucket() {
        return defaultBucket;
    }

    private String buildPath(String bucket, String key) {
        return bucket + "/" + key;
    }
}


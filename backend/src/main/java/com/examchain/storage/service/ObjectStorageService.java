package com.examchain.storage.service;

public interface ObjectStorageService {

    void putObject(String bucket, String objectKey, byte[] data, String contentType);

    byte[] getObject(String bucket, String objectKey);

    boolean exists(String bucket, String objectKey);

    void deleteObject(String bucket, String objectKey);
}


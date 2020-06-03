package com.cc.cloud.drive.aws.s3;

public class S3Exception extends RuntimeException {
    public S3Exception(String message) {
        super(message);
    }

    public S3Exception(String message, Exception e) {
        super(message, e);
    }
}

package com.cc.cloud.drive.aws.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.IOUtils;
import com.cc.cloud.drive.api.dto.UserBucketDto;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class S3StorageService {
    private AmazonS3 s3Client;
    private String bucket;

    public S3StorageService(AmazonS3 s3Client, @Value("${app.awsServices.bucketName}") String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    public void store(UserBucketDto userBucketDto, MultipartFile file) {
        try {
            ObjectMetadata objectMetadata = new ObjectMetadata();

            objectMetadata.setContentLength(file.getSize());
            s3Client.putObject(bucket,
                    computeKeyFake(userBucketDto) + file.getOriginalFilename(),
                    file.getInputStream(), objectMetadata);
        } catch (Exception e) {
            throw new S3Exception("Failed to upload file " + file.getOriginalFilename() + " to S3", e);
        }
    }

    public List<String> findFiles(UserBucketDto userBucketDto) {
        String prefixKey = computeKeyFake(userBucketDto);
        ObjectListing listing = s3Client.listObjects(bucket, prefixKey);
        List<S3ObjectSummary> summaries = listing.getObjectSummaries();

        while (listing.isTruncated()) {
            listing = s3Client.listNextBatchOfObjects(listing);
            summaries.addAll(listing.getObjectSummaries());
        }

        return summaries.stream()
                .filter(s3ObjectSummary -> !s3ObjectSummary.getKey().equals(prefixKey))
                .map(s3ObjectSummary -> getFileName(s3ObjectSummary, prefixKey))
                .collect(toList());
    }

    //TODO remove this and use the other function
    private String computeKeyFake(UserBucketDto userBucketDto) {
        return userBucketDto.getOwnerUsername() + "_" + userBucketDto.getName() + "/";
    }

    private String computeKey(UserBucketDto userBucketDto) {
        return DigestUtils.sha256Hex(userBucketDto.getName() + userBucketDto.getOwnerUsername());
    }

    private String getFileName(S3ObjectSummary s3ObjectSummary, String prefixKey) {
        return s3ObjectSummary.getKey().replace(prefixKey, "");
    }

    public void delete(UserBucketDto userBucketDto, String fileName) {
        try {
            s3Client.deleteObject(bucket, computeKeyFake(userBucketDto) + fileName);
        } catch (Exception e) {
            throw new S3Exception("Failed to delete file " + fileName + " from S3", e);
        }
    }

    public byte[] retrieve(UserBucketDto userBucketDto, String fileName) {
        try {
            S3Object object = s3Client.getObject(bucket, computeKeyFake(userBucketDto) + fileName);

            return IOUtils.toByteArray(object.getObjectContent());
        } catch (Exception e) {
            throw new S3Exception("Failed to upload file " + fileName + " to S3");
        }
    }
}

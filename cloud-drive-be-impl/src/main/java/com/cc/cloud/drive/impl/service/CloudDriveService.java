package com.cc.cloud.drive.impl.service;

import com.cc.cloud.drive.api.dto.UserBucketDto;
import com.cc.cloud.drive.api.dto.UserDto;
import com.cc.cloud.drive.aws.s3.MessageUpdate;
import com.cc.cloud.drive.aws.s3.S3StorageService;
import com.cc.cloud.drive.aws.s3.SQSService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class CloudDriveService {

    private UserBucketService userBucketService;
    private AccessRuleService accessRuleService;
    private S3StorageService s3StorageService;
    private SQSService sqsService;
    private ObjectMapper objectMapper;

    public CloudDriveService(UserBucketService userBucketService,
                             AccessRuleService accessRuleService,
                             S3StorageService s3StorageService,
                             SQSService sqsService,
                             ObjectMapper objectMapper) {
        this.userBucketService = userBucketService;
        this.accessRuleService = accessRuleService;
        this.s3StorageService = s3StorageService;
        this.sqsService = sqsService;
        this.objectMapper = objectMapper;
    }

    public void uploadFile(long bucketId, MultipartFile file) {
        UserBucketDto userBucketDto = userBucketService.getById(bucketId)
                .orElseThrow(() -> new RuntimeException("No bucket exists with id " + bucketId));
        s3StorageService.store(userBucketDto, file);

        String message = String.format("File %s has been uploaded in bucket %s", file.getOriginalFilename(), userBucketDto.getName());
        List<MessageUpdate> messageUpdates = accessRuleService.getAllUsersForBucketId(userBucketDto.getOwnerUsername(), userBucketDto.getId())
                .stream()
                .map(UserDto::getSnsTopicArn)
                .map(arn -> new MessageUpdate(arn, message))
                .collect(toList());

        try {
            for (MessageUpdate messageUpdate : messageUpdates)
                sqsService.send(objectMapper.writeValueAsString(messageUpdate));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] downloadFile(long bucketId, String fileName) {
        UserBucketDto userBucketDto = userBucketService.getById(bucketId)
                .orElseThrow(() -> new RuntimeException("No bucket exists with id " + bucketId));
        return s3StorageService.retrieve(userBucketDto, fileName);
    }

    public void deleteFile(long bucketId, String fileName) {
        UserBucketDto userBucketDto = userBucketService.getById(bucketId)
                .orElseThrow(() -> new RuntimeException("No bucket exists with id " + bucketId));

        s3StorageService.delete(userBucketDto, fileName);

        String message = String.format("Files %s has been deleted from bucket %s", fileName, userBucketDto.getName());
        List<MessageUpdate> messageUpdates = accessRuleService.getAllUsersForBucketId(userBucketDto.getOwnerUsername(), userBucketDto.getId())
                .stream()
                .map(UserDto::getSnsTopicArn)
                .map(arn -> new MessageUpdate(arn, message))
                .collect(toList());

        try {
            for (MessageUpdate messageUpdate : messageUpdates)
                sqsService.send(objectMapper.writeValueAsString(messageUpdate));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

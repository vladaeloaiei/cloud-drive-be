package com.cc.cloud.drive.impl.service;

import com.cc.cloud.drive.api.dto.AccessRuleDto;
import com.cc.cloud.drive.api.dto.UserBucketDto;
import com.cc.cloud.drive.api.dto.UserDto;
import com.cc.cloud.drive.aws.s3.MessageUpdate;
import com.cc.cloud.drive.aws.s3.S3StorageService;
import com.cc.cloud.drive.aws.s3.SQSService;
import com.cc.cloud.drive.impl.model.UserBucket;
import com.cc.cloud.drive.impl.repository.UserBucketRepository;
import com.cc.cloud.drive.security.user.User;
import com.cc.cloud.drive.security.user.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
public class UserBucketService {
    private UserBucketRepository userBucketRepository;
    private AccessRuleService accessRuleService;
    private S3StorageService s3StorageService;
    private UserService userService;
    private ModelMapper modelMapper;
    private SQSService sqsService;
    private ObjectMapper objectMapper;

    @Autowired
    public UserBucketService(UserBucketRepository userBucketRepository,
                             AccessRuleService accessRuleService,
                             S3StorageService s3StorageService,
                             UserService userService,
                             ModelMapper modelMapper,
                             SQSService sqsService,
                             ObjectMapper objectMapper) {
        this.userBucketRepository = userBucketRepository;
        this.accessRuleService = accessRuleService;
        this.s3StorageService = s3StorageService;
        this.userService = userService;
        this.modelMapper = modelMapper;
        this.sqsService = sqsService;
        this.objectMapper = objectMapper;
    }

    public Optional<UserBucketDto> getById(Long id) {
        Optional<UserBucket> userBucket = userBucketRepository.findById(id);

        return userBucket.map(x -> modelMapper.map(x, UserBucketDto.class));
    }

    public List<UserBucketDto> getByOwnerUsername(String ownerUsername) {
        List<UserBucket> userBuckets = userBucketRepository.findByOwnerUsername(ownerUsername);
        List<UserBucketDto> userBucketDtos = userBuckets.stream()
                .map(x -> modelMapper.map(x, UserBucketDto.class))
                .collect(toList());

        return userBucketDtos.stream()
                .map(this::populateBucket)
                .collect(toList());
    }

    private UserBucketDto populateBucket(UserBucketDto userBucketDto) {
        userBucketDto.setFiles(s3StorageService.findFiles(userBucketDto));

        return userBucketDto;
    }

    public List<UserBucketDto> getByGranteeUsername(String granteeUsername) {
        List<AccessRuleDto> accessRules = accessRuleService.getByGranteeUsername(granteeUsername);
        List<UserBucketDto> userBucketDtos = accessRules.stream()
                .map(AccessRuleDto::getUserBucket)
                .map(x -> modelMapper.map(x, UserBucketDto.class))
                .collect(toList());

        return userBucketDtos.stream()
                .map(this::populateBucket)
                .collect(toList());
    }

    @Transactional
    public UserBucketDto create(String ownerUsername, String bucketName) {
        Optional<UserBucket> currentUserBucket = userBucketRepository.findByNameAndOwnerUsername(
                bucketName, ownerUsername);

        currentUserBucket.ifPresent(u -> {
            throw new RuntimeException("This bucket already exists!");
        });

        UserBucket userBucket = new UserBucket();

        userBucket.setName(bucketName);
        userBucket.setOwner(modelMapper.map(
                userService.getSensitiveByUsername(ownerUsername).orElseThrow(() -> new RuntimeException("No user :" + ownerUsername)),
                User.class));

        UserBucketDto savedUserBucket = modelMapper.map(userBucketRepository.save(userBucket), UserBucketDto.class);
        String message = String.format("Bucket %s has been created", savedUserBucket.getName());
        List<MessageUpdate> messageUpdates = accessRuleService.getAllUsersForBucketId(ownerUsername, userBucket.getId())
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

        return savedUserBucket;
    }

    @Transactional
    public AccessRuleDto shareBucket(String ownerUsername, String bucketName, String granteeUsername) {
        UserBucket userBucket = userBucketRepository.findByNameAndOwnerUsername(bucketName, ownerUsername)
                .orElseThrow(() -> new RuntimeException("Bucket does not exists: " + bucketName));

        AccessRuleDto accessRuleDto = accessRuleService.grantAccess(userBucket, granteeUsername);

        String message = String.format("Bucket %s has been shared to %s", bucketName, granteeUsername);
        List<MessageUpdate> messageUpdates = accessRuleService.getAllUsersForBucketId(ownerUsername, userBucket.getId())
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

        return accessRuleDto;
    }

    @Transactional
    public void delete(String ownerUsername, String bucketName) {
        UserBucket userBucket = userBucketRepository.findByNameAndOwnerUsername(bucketName, ownerUsername)
                .orElseThrow(() -> new RuntimeException("Bucket does not exists: " + bucketName));

        userBucketRepository.deleteById(userBucket.getId());

        String message = String.format("Bucket %s has been deleted", bucketName);
        List<MessageUpdate> messageUpdates = accessRuleService.getAllUsersForBucketId(ownerUsername, userBucket.getId())
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

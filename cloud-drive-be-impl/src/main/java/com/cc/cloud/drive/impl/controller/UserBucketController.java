package com.cc.cloud.drive.impl.controller;

import com.cc.cloud.drive.api.dto.AccessRuleDto;
import com.cc.cloud.drive.api.dto.UserBucketDto;
import com.cc.cloud.drive.aws.s3.S3Exception;
import com.cc.cloud.drive.impl.service.UserBucketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RequestMapping("/user/bucket")
@RestController
public class UserBucketController {
    private UserBucketService userBucketService;

    @Autowired
    public UserBucketController(UserBucketService userBucketService) {
        this.userBucketService = userBucketService;
    }

    @GetMapping("{id}")
    public ResponseEntity<UserBucketDto> getById(@PathVariable("id") Long id) {
        Optional<UserBucketDto> userBucketDto = userBucketService.getById(id);

        return userBucketDto.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/owner/{ownerUsername}")
    public ResponseEntity<List<UserBucketDto>> getByOwnerUsername(@PathVariable("ownerUsername") String ownerUsername) {
        List<UserBucketDto> userBucketsDto = userBucketService.getByOwnerUsername(ownerUsername);

        return ResponseEntity.ok(userBucketsDto);
    }

    @PostMapping("/owner/{ownerUsername}/{bucketName}")
    public ResponseEntity<UserBucketDto> create(@PathVariable("ownerUsername") String ownerUsername,
                                                @PathVariable("bucketName") String bucketName) {
        return ResponseEntity.ok(userBucketService.create(ownerUsername, bucketName));
    }

    @DeleteMapping("/owner/{ownerUsername}/{bucketName}")
    public ResponseEntity<UserBucketDto> delete(@PathVariable("ownerUsername") String ownerUsername,
                                                @PathVariable("bucketName") String bucketName) {
        userBucketService.delete(ownerUsername, bucketName);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/grantee/{granteeUsername}")
    public ResponseEntity<List<UserBucketDto>> getByGranteeUsername(@PathVariable("granteeUsername") String granteeUsername) {
        List<UserBucketDto> userBucketsDto = userBucketService.getByGranteeUsername(granteeUsername);

        return ResponseEntity.ok(userBucketsDto);
    }

    @PostMapping("/grantee/{granteeUsername}/{bucketName}")
    public ResponseEntity<AccessRuleDto> shareBucket(@PathVariable("granteeUsername") String granteeUsername,
                                                     @PathVariable("bucketName") String bucketName,
                                                     @RequestParam("ownerUsername") String ownerUsername) {
        return ResponseEntity.ok(userBucketService.shareBucket(ownerUsername, bucketName, granteeUsername));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(S3Exception e) {
        e.printStackTrace();

        return ResponseEntity.badRequest().build();
    }
}

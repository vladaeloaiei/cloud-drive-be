package com.cc.cloud.drive.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccessRuleDto {
    private Long id;
    private UserBucketDto userBucket;
    private UserDto granteeId;
}

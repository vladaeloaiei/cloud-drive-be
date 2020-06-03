package com.cc.cloud.drive.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserBucketDto {
    private Long id;
    private String name;
    private String ownerUsername;
    private List<String> files;
}

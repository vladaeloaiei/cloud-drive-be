package com.cc.cloud.drive.impl.service;

import com.cc.cloud.drive.api.dto.AccessRuleDto;
import com.cc.cloud.drive.api.dto.UserDto;
import com.cc.cloud.drive.impl.model.AccessRule;
import com.cc.cloud.drive.impl.model.UserBucket;
import com.cc.cloud.drive.impl.repository.AccessRuleRepository;
import com.cc.cloud.drive.security.user.User;
import com.cc.cloud.drive.security.user.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
@Transactional
public class AccessRuleService {
    private AccessRuleRepository accessRuleRepository;
    private UserService userService;
    private ModelMapper modelMapper;

    @Autowired
    public AccessRuleService(AccessRuleRepository accessRuleRepository,
                             UserService userService,
                             ModelMapper modelMapper) {
        this.accessRuleRepository = accessRuleRepository;
        this.userService = userService;
        this.modelMapper = modelMapper;
    }

    public Optional<AccessRuleDto> getById(Long id) {
        Optional<AccessRule> accessRule = accessRuleRepository.findById(id);

        return accessRule.map(x -> modelMapper.map(x, AccessRuleDto.class));
    }

    public List<AccessRuleDto> getByUserBucketId(Long userBucketId) {
        List<AccessRule> accessRule = accessRuleRepository.findByUserBucketId(userBucketId);

        return accessRule.stream()
                .map(x -> modelMapper.map(x, AccessRuleDto.class))
                .collect(toList());
    }

    public List<AccessRuleDto> getByGranteeUsername(String granteeUsername) {
        List<AccessRule> accessRule = accessRuleRepository.findByGranteeUsername(granteeUsername);

        return accessRule.stream()
                .map(x -> modelMapper.map(x, AccessRuleDto.class))
                .collect(toList());
    }

    public AccessRuleDto grantAccess(UserBucket userBucket, String username) {
        AccessRule accessRule = new AccessRule();

        accessRule.setUserBucket(userBucket);
        accessRule.setGrantee(modelMapper.map(userService.getSensitiveByUsername(username)
                .orElseThrow(() -> new RuntimeException("No user :" + username)), User.class));

        return modelMapper.map(accessRuleRepository.save(accessRule), AccessRuleDto.class);
    }

    public List<UserDto> getAllUsersForBucketId(String ownerUsername, Long bucketId) {
        List<AccessRuleDto> accessRuleDtos = getByUserBucketId(bucketId);

        List<UserDto> users = accessRuleDtos.stream()
                .map(rule -> userService.getById(rule.getGranteeId().getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
        UserDto owner = userService.getByUsername(ownerUsername).orElseThrow(() -> new RuntimeException());
        users.add(owner);

        return users;
    }
}

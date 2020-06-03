package com.cc.cloud.drive.impl.repository;

import com.cc.cloud.drive.impl.model.AccessRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccessRuleRepository extends JpaRepository<AccessRule, Long> {
    Optional<AccessRule> findById(Long id);

    List<AccessRule> findByUserBucketId(Long userBucketId);

    List<AccessRule> findByGranteeId(Long granteeId);

    List<AccessRule> findByGranteeUsername(String granteeUsername);
}

package com.cc.cloud.drive.impl.repository;

import com.cc.cloud.drive.impl.model.UserBucket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBucketRepository extends JpaRepository<UserBucket, Long> {
    Optional<UserBucket> findById(Long id);

    List<UserBucket> findByOwnerUsername(String ownerUsername);

    Optional<UserBucket> findByNameAndOwnerUsername(String name, String ownerUsername);
}

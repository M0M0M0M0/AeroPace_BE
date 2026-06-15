package com.group1.aeropace.repository;

import com.group1.aeropace.entity.CustomerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, Long> {
    Optional<CustomerProfile> findByUser_Id(Long userId);
    List<CustomerProfile> findByUser_IdIn(List<Long> userIds);
}
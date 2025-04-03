package com.hmshk.repository;

import com.hmshk.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
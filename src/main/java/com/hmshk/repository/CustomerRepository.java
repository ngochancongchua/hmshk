package com.hmshk.repository;

import com.hmshk.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
    List<Customer> findByNameContainingIgnoreCase(String name);
    List<Customer> findByPhoneNumberContaining(String phoneNumber);
    List<Customer> findByVipStatus(String vipStatus);
    List<Customer> findByActive(boolean active);
}
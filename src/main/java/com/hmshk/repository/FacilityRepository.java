package com.hmshk.repository;

import com.hmshk.model.Facility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, Long> {
    List<Facility> findByAvailable(boolean available);
    List<Facility> findByNameContainingIgnoreCase(String name);
}
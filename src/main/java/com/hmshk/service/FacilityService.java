package com.hmshk.service;

import com.hmshk.model.Facility;
import com.hmshk.repository.FacilityRepository;
import com.hmshk.repository.RoomRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FacilityService {
    
    private final FacilityRepository facilityRepository;
    
    
    public FacilityService(FacilityRepository facilityRepository) {
        this.facilityRepository = facilityRepository;
    }
    
    
    public List<Facility> getAllFacilities() {
        return facilityRepository.findAll();
    }
    
    public Optional<Facility> getFacilityById(Long id) {
        return facilityRepository.findById(id);
    }
    
    public List<Facility> getAvailableFacilities() {
        return facilityRepository.findByAvailable(true);
    }
    
    public List<Facility> searchFacilities(String name) {
        return facilityRepository.findByNameContainingIgnoreCase(name);
    }
    
    public Facility addFacility(Facility facility) {
        return facilityRepository.save(facility);
    }
    
    public Facility updateFacility(Facility facility) {
        Optional<Facility> existingFacility = facilityRepository.findById(facility.getId());
        if (existingFacility.isPresent()) {
            return facilityRepository.save(facility);
        } else {
            throw new RuntimeException("Facility not found");
        }
    }
    
    public void deleteFacility(Long id) {
        facilityRepository.deleteById(id);
    }
    
    public void setFacilityAvailability(Long facilityId, boolean isAvailable) {
        Optional<Facility> facilityOpt = facilityRepository.findById(facilityId);
        if (facilityOpt.isPresent()) {
            Facility facility = facilityOpt.get();
            facility.setAvailable(isAvailable);
            facilityRepository.save(facility);
        } else {
            throw new RuntimeException("Facility not found");
        }
    }
}
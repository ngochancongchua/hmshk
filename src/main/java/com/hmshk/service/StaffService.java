package com.hmshk.service;

import com.hmshk.model.Role;
import com.hmshk.model.Staff;
import com.hmshk.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StaffService {
    
    private final StaffRepository staffRepository;
    
    public StaffService(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }
    
    
    public Staff registerStaff(Staff staff) {
        if (staffRepository.existsByUsername(staff.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (staffRepository.existsByEmail(staff.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        return staffRepository.save(staff);
    }
    
    public Optional<Staff> findByUsername(String username) {
        return staffRepository.findByUsername(username);
    }
    
    /*public Staff authenticateStaff(String username, String password) {
        Optional<Staff> staffOpt = staffRepository.findByUsername(username);
        if (staffOpt.isPresent()) {
            Staff staff = staffOpt.get();
            if (password.equals(staff.getPassword())) {
                return staff;
            }
        }
        throw new RuntimeException("Invalid credentials");
    }*/
    public Staff authenticateStaff(String username, String password) {
        System.out.println("Attempting to authenticate user: " + username);
        Optional<Staff> staffOpt = staffRepository.findByUsername(username);
        
        if (staffOpt.isPresent()) {
            Staff staff = staffOpt.get();
            System.out.println("Found user: " + staff.getUsername() + " with ID: " + staff.getId());
            System.out.println("Stored password: [" + staff.getPassword() + "]");
            System.out.println("Provided password: [" + password + "]");
            
            if (password.equals(staff.getPassword())) {
                System.out.println("✅ Password matched, authentication successful!");
                return staff;
            } else {
                System.out.println("❌ Password did not match");
            }
        } else {
            System.out.println("❌ Username not found: " + username);
        }
        
        throw new RuntimeException("Invalid credentials");
    }
    
    
    public List<Staff> getAllStaff() {
        return staffRepository.findAll();
    }
    
    public Optional<Staff> getStaffById(Long id) {
        return staffRepository.findById(id);
    }
    
    public Staff updateStaff(Staff staff) {
        Optional<Staff> existingStaff = staffRepository.findById(staff.getId());
        if (existingStaff.isPresent()) {
            return staffRepository.save(staff);
        } else {
            throw new RuntimeException("Staff not found");
        }
    }
    
    public void deleteStaff(Long id) {
        staffRepository.deleteById(id);
    }
}

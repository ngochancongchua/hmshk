package com.hmshk.controller;

import com.hmshk.model.Role;
import com.hmshk.model.Staff;
import com.hmshk.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/staff-dashboard")
public class StaffDashboardController {
    
    private final StaffService staffService;
    
    // 有些debugger suggest不需要的，但絕對要的！不可以刪！
    public StaffDashboardController(StaffService staffService) {
        this.staffService = staffService;
    }

    
    /**
     * Display the staff dashboard with all staff members and role statistics
     */
    /**
     * Simple version of the staff dashboard for testing
     */
    @GetMapping("/simple")
    public String simpleStaffDashboard(Model model, HttpSession session) {
        // Check if user is logged in
        if (session.getAttribute("staff") == null) {
            return "redirect:/login";
        }
        
        // Get all staff
        List<Staff> staffList = staffService.getAllStaff();
        model.addAttribute("staffList", staffList);
        
        return "simple-staff-dashboard";
    }
    
    /**
     * Search and filter staff members
     */
    @GetMapping("/search")
    public String searchStaff(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String role,
            Model model, 
            HttpSession session) {
        
        // Check if user is admin or manager
        Staff currentUser = (Staff) session.getAttribute("staff");
        if (currentUser == null || (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.MANAGER)) {
            return "redirect:/dashboard";
        }
        
        // Get all staff
        List<Staff> allStaff = staffService.getAllStaff();
        
        // Apply filters if provided
        List<Staff> filteredStaff = allStaff.stream()
            .filter(staff -> name == null || name.isEmpty() || 
                    staff.getName().toLowerCase().contains(name.toLowerCase()))
            .filter(staff -> role == null || role.isEmpty() || 
                    staff.getRole().toString().equals(role))
            .collect(Collectors.toList());
        
        // Calculate role statistics based on all staff
        Map<String, Long> roleStats = calculateRoleStats(allStaff);
        
        model.addAttribute("staffList", filteredStaff);
        model.addAttribute("roleStats", roleStats);
        model.addAttribute("searchName", name);
        model.addAttribute("searchRole", role);
        
        return "staff-dashboard";
    }
    
    /**
     * Add a new staff member
     */
    @PostMapping("/add")
    public String addStaff(@ModelAttribute Staff staff, RedirectAttributes redirectAttributes, HttpSession session) {
        // Check if user is admin or manager
        Staff currentUser = (Staff) session.getAttribute("staff");
        if (currentUser == null || (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.MANAGER)) {
            return "redirect:/dashboard";
        }
        
        try {
            // Register the new staff member
            staffService.registerStaff(staff);
            redirectAttributes.addFlashAttribute("success", "Staff member created successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating staff member: " + e.getMessage());
        }
        
        return "redirect:/staff-dashboard";
    }
    
    /**
     * Update an existing staff member
     */
    @PostMapping("/update")
    public String updateStaff(@ModelAttribute Staff staff, RedirectAttributes redirectAttributes, HttpSession session) {
        // Check if user is admin or manager
        Staff currentUser = (Staff) session.getAttribute("staff");
        if (currentUser == null || (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.MANAGER)) {
            return "redirect:/dashboard";
        }
        
        try {
            // Check if password is empty (no change)
            if (staff.getPassword() == null || staff.getPassword().isEmpty()) {
                // Get the current password from the database and set it
                Staff existingStaff = staffService.getStaffById(staff.getId()).orElseThrow(
                    () -> new RuntimeException("Staff not found"));
                staff.setPassword(existingStaff.getPassword());
            }
            
            // Update the staff member
            staffService.updateStaff(staff);
            redirectAttributes.addFlashAttribute("success", "Staff member updated successfully");
            
            // If the user updated their own account, update the session
            if (currentUser.getId().equals(staff.getId())) {
                session.setAttribute("staff", staff);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating staff member: " + e.getMessage());
        }
        
        return "redirect:/staff-dashboard";
    }
    
    /**
     * Delete a staff member
     */
    @GetMapping("/delete/{id}")
    public String deleteStaff(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        // Check if user is admin or manager
        Staff currentUser = (Staff) session.getAttribute("staff");
        if (currentUser == null || (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.MANAGER)) {
            return "redirect:/dashboard";
        }
        
        // Prevent users from deleting their own account
        if (currentUser.getId().equals(id)) {
            redirectAttributes.addFlashAttribute("error", "You cannot delete your own account");
            return "redirect:/staff-dashboard";
        }
        
        try {
            staffService.deleteStaff(id);
            redirectAttributes.addFlashAttribute("success", "Staff member deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting staff member: " + e.getMessage());
        }
        
        return "redirect:/staff-dashboard";
    }
    
    /**
     * Helper method to calculate statistics about staff roles
     */
    private Map<String, Long> calculateRoleStats(List<Staff> staffList) {
        Map<String, Long> roleStats = new HashMap<>();
        
        // Initialize counts for all roles
        for (Role role : Role.values()) {
            roleStats.put(role.toString(), 0L);
        }
        
        // Count staff by role
        for (Staff staff : staffList) {
            String roleKey = staff.getRole().toString();
            roleStats.put(roleKey, roleStats.getOrDefault(roleKey, 0L) + 1);
        }
        
        return roleStats;
    }
}
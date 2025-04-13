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
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class StaffController {
    
    private final StaffService staffService;
    
    //added
    // 有些debugger suggest不需要的，但絕對要的！不可以刪！
    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }
    //end
    
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
    
    @PostMapping("/login")
    public String login(@RequestParam String username, 
                        @RequestParam String password,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        try {
            Staff staff = staffService.authenticateStaff(username, password);
            session.setAttribute("staff", staff);
            return "redirect:/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Invalid username or password");
            return "redirect:/login";
        }
    }
    
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("roles", Role.values());
        return "register";
    }
    
    @PostMapping("/register")
    public String register(@ModelAttribute Staff staff, RedirectAttributes redirectAttributes, HttpSession session) {
        try {
            staffService.registerStaff(staff);
            
            // Check if user is already logged in (staff page registration)
            if (session.getAttribute("staff") != null) {
                redirectAttributes.addFlashAttribute("success", "Staff registered successfully");
                return "redirect:/staff"; // Redirect back to staff page
            } else {
                // For standalone registration page
                redirectAttributes.addFlashAttribute("success", "Staff registered successfully");
                return "redirect:/login";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            
            // Check if user is already logged in (staff page registration)
            if (session.getAttribute("staff") != null) {
                return "redirect:/staff"; // Redirect back to staff page
            } else {
                return "redirect:/register";
            }
        }
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
    
    @GetMapping("/staff")
    public String staffPage(Model model) {
        List<Staff> staffList = staffService.getAllStaff();
        model.addAttribute("staffList", staffList);
        model.addAttribute("roles", Role.values());
        return "staff";
    }
    
    @GetMapping("/staff/{id}")
    public String getStaff(@PathVariable Long id, Model model) {
        Optional<Staff> staff = staffService.getStaffById(id);
        if (staff.isPresent()) {
            model.addAttribute("staff", staff.get());
            model.addAttribute("roles", Role.values());
            return "staff-edit";
        } else {
            return "redirect:/staff";
        }
    }
    
    @PostMapping("/staff/update")
    public String updateStaff(@ModelAttribute Staff staff, RedirectAttributes redirectAttributes) {
        try {
            staffService.updateStaff(staff);
            redirectAttributes.addFlashAttribute("success", "Staff updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/staff";
    }
    
    @GetMapping("/staff/delete/{id}")
    public String deleteStaff(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            staffService.deleteStaff(id);
            redirectAttributes.addFlashAttribute("success", "Staff deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/staff";
    }
}

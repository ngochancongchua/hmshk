package com.hmshk.controller;

import com.hmshk.model.Facility;
import com.hmshk.service.FacilityService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@Controller
public class FacilityController {
    
    private final FacilityService facilityService;
    
    public FacilityController(FacilityService facilityService) {
        this.facilityService = facilityService;
    }
    
    @GetMapping("/facilities")
    public String facilitiesPage(Model model, HttpSession session) {
        if (session.getAttribute("staff") == null) {
            return "redirect:/login";
        }
        
        List<Facility> facilities = facilityService.getAllFacilities();
        model.addAttribute("facilities", facilities);
        return "facility";
    }
    
    @GetMapping("/facilities/{id}")
    public String getFacilityById(@PathVariable Long id, Model model, HttpSession session) {
        if (session.getAttribute("staff") == null) {
            return "redirect:/login";
        }
        
        Optional<Facility> facility = facilityService.getFacilityById(id);
        if (facility.isPresent()) {
            model.addAttribute("facility", facility.get());
            return "facility-edit";
        } else {
            return "redirect:/facilities";
        }
    }
    
    @PostMapping("/facilities/add")
    public String addFacility(@ModelAttribute Facility facility, RedirectAttributes redirectAttributes) {
        try {
            facilityService.addFacility(facility);
            redirectAttributes.addFlashAttribute("success", "Facility added successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/facilities";
    }
    
    @PostMapping("/facilities/update")
    public String updateFacility(@ModelAttribute Facility facility, RedirectAttributes redirectAttributes) {
        try {
            facilityService.updateFacility(facility);
            redirectAttributes.addFlashAttribute("success", "Facility updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/facilities";
    }
    
    @GetMapping("/facilities/delete/{id}")
    public String deleteFacility(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            facilityService.deleteFacility(id);
            redirectAttributes.addFlashAttribute("success", "Facility deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/facilities";
    }
    
    @PostMapping("/facilities/{id}/toggle-availability")
    public String toggleFacilityAvailability(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Facility> facilityOpt = facilityService.getFacilityById(id);
            if (facilityOpt.isPresent()) {
                Facility facility = facilityOpt.get();
                facilityService.setFacilityAvailability(id, !facility.isAvailable());
                redirectAttributes.addFlashAttribute("success", 
                        "Facility availability updated: " + (!facility.isAvailable() ? "Available" : "Unavailable"));
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/facilities";
    }
    
    @GetMapping("/facilities/search")
    public String searchFacilities(@RequestParam String name, Model model, HttpSession session) {
        if (session.getAttribute("staff") == null) {
            return "redirect:/login";
        }
        
        List<Facility> facilities = facilityService.searchFacilities(name);
        model.addAttribute("facilities", facilities);
        model.addAttribute("searchQuery", name);
        return "facility";
    }
}
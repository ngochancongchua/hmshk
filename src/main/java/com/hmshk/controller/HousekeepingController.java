package com.hmshk.controller;

import com.hmshk.model.Room;
import com.hmshk.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/housekeeping")
public class HousekeepingController {
    
    private final RoomService roomService;
    
    // Constructor for when lombok is not being used (e.g. in Eclipse)
    public HousekeepingController(RoomService roomService) {
        this.roomService = roomService;
    }
    
    @GetMapping
    public String housekeepingPage(Model model, HttpSession session) {
        if (session.getAttribute("staff") == null) {
            return "redirect:/login";
        }
        
        // Get all rooms that need cleaning
        List<Room> allRooms = roomService.getAllRooms();
        List<Room> roomsNeedCleaning = allRooms.stream()
                .filter(Room::isNeedsCleaning)
                .collect(Collectors.toList());
        
        // Get available rooms that need cleaning
        List<Room> availableRoomsNeedCleaning = roomsNeedCleaning.stream()
                .filter(Room::isAvailable)
                .collect(Collectors.toList());
        
        // Get occupied rooms that need cleaning
        List<Room> occupiedRoomsNeedCleaning = roomsNeedCleaning.stream()
                .filter(room -> !room.isAvailable())
                .collect(Collectors.toList());
        
        model.addAttribute("roomsNeedCleaning", roomsNeedCleaning);
        model.addAttribute("availableRoomsNeedCleaning", availableRoomsNeedCleaning);
        model.addAttribute("occupiedRoomsNeedCleaning", occupiedRoomsNeedCleaning);
        model.addAttribute("totalRooms", allRooms.size());
        model.addAttribute("totalNeedCleaning", roomsNeedCleaning.size());
        
        return "housekeeping";
    }
    
    @PostMapping("/mark-cleaned/{id}")
    public String markRoomAsCleaned(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            roomService.setRoomCleaning(id, false);
            redirectAttributes.addFlashAttribute("success", "Room marked as cleaned successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/housekeeping";
    }
    
    @PostMapping("/mark-needs-cleaning/{id}")
    public String markRoomAsNeedsCleaning(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            roomService.setRoomCleaning(id, true);
            redirectAttributes.addFlashAttribute("success", "Room marked as needs cleaning successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/housekeeping";
    }
    
    @PostMapping("/bulk-mark-cleaned")
    public String bulkMarkRoomsCleaned(@RequestParam List<Long> roomIds, RedirectAttributes redirectAttributes) {
        try {
            for (Long id : roomIds) {
                roomService.setRoomCleaning(id, false);
            }
            redirectAttributes.addFlashAttribute("success", roomIds.size() + " rooms marked as cleaned successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/housekeeping";
    }
}
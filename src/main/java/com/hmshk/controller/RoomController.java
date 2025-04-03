package com.hmshk.controller;

import com.hmshk.model.Room;
import com.hmshk.model.RoomType;
import com.hmshk.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class RoomController {
    
    private final RoomService roomService;
    
    //added

    //end
    
    @GetMapping("/rooms")
    public String roomsPage(Model model, HttpSession session) {
        if (session.getAttribute("staff") == null) {
            return "redirect:/login";
        }
        
        List<Room> rooms = roomService.getAllRooms();
        model.addAttribute("rooms", rooms);
        model.addAttribute("roomTypes", RoomType.values());
        return "room";
    }
    
    @GetMapping("/rooms/{id}")
    public String getRoomById(@PathVariable Long id, Model model, HttpSession session) {
        if (session.getAttribute("staff") == null) {
            return "redirect:/login";
        }
        
        Optional<Room> room = roomService.getRoomById(id);
        if (room.isPresent()) {
            model.addAttribute("room", room.get());
            model.addAttribute("roomTypes", RoomType.values());
            return "room-edit";
        } else {
            return "redirect:/rooms";
        }
    }
    
    @PostMapping("/rooms/add")
    public String addRoom(@ModelAttribute Room room, RedirectAttributes redirectAttributes) {
        try {
            roomService.addRoom(room);
            redirectAttributes.addFlashAttribute("success", "Room added successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/rooms";
    }
    
    @PostMapping("/rooms/update")
    public String updateRoom(@ModelAttribute Room room, RedirectAttributes redirectAttributes) {
        try {
            roomService.updateRoom(room);
            redirectAttributes.addFlashAttribute("success", "Room updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/rooms";
    }
    
    @GetMapping("/rooms/delete/{id}")
    public String deleteRoom(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            roomService.deleteRoom(id);
            redirectAttributes.addFlashAttribute("success", "Room deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/rooms";
    }
    
    @GetMapping("/rooms/available")
    public String getAvailableRooms(
            @RequestParam(required = false) LocalDate checkIn,
            @RequestParam(required = false) LocalDate checkOut,
            Model model) {
        
        List<Room> availableRooms;
        if (checkIn != null && checkOut != null) {
            availableRooms = roomService.getAvailableRoomsForDates(checkIn, checkOut);
        } else {
            availableRooms = roomService.getAvailableRooms();
        }
        
        model.addAttribute("rooms", availableRooms);
        model.addAttribute("checkIn", checkIn);
        model.addAttribute("checkOut", checkOut);
        
        return "room-available";
    }
    
    @PostMapping("/rooms/{id}/toggle-cleaning")
    public String toggleRoomCleaning(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Room> roomOpt = roomService.getRoomById(id);
            if (roomOpt.isPresent()) {
                Room room = roomOpt.get();
                roomService.setRoomCleaning(id, !room.isNeedsCleaning());
                redirectAttributes.addFlashAttribute("success", 
                        "Room cleaning status updated: " + (!room.isNeedsCleaning() ? "Needs cleaning" : "Cleaned"));
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/rooms";
    }
}

package com.hmshk.controller;

import com.hmshk.model.Booking;
import com.hmshk.model.BookingStatus;
import com.hmshk.model.Room;
import com.hmshk.service.BookingService;
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
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/checkin-checkout")
public class CheckinCheckoutController {
    
    private final BookingService bookingService;
    private final RoomService roomService;
    
    //add for eclipse
    public CheckinCheckoutController(BookingService bookingService, RoomService roomService) {
        this.bookingService = bookingService;
        this.roomService = roomService;
    }
    //end
    
    
    @GetMapping
    public String checkInCheckOutPage(Model model, HttpSession session) {
        if (session.getAttribute("staff") == null) {
            return "redirect:/login";
        }
        
        LocalDate today = LocalDate.now();
        
        // Get today's check-ins (bookings with today's check-in date and status RESERVED)
        List<Booking> todayCheckins = bookingService.getBookingsByDateRange(today, today)
            .stream()
            .filter(b -> b.getStatus() == BookingStatus.RESERVED || b.getStatus() == BookingStatus.CHECKED_IN)
            .collect(Collectors.toList());
        
        // Get today's check-outs (bookings with today's check-out date)
        List<Booking> todayCheckouts = bookingService.getTodayCheckouts()
            .stream()
            .filter(b -> b.getStatus() == BookingStatus.CHECKED_IN || b.getStatus() == BookingStatus.CHECKED_OUT)
            .collect(Collectors.toList());
        
        // Get currently staying guests (status CHECKED_IN)
        List<Booking> currentGuests = bookingService.getBookingsByStatus(BookingStatus.CHECKED_IN);
        
        // Debug info
        System.out.println("Current guests found: " + (currentGuests != null ? currentGuests.size() : "null"));
        if (currentGuests != null && !currentGuests.isEmpty()) {
            System.out.println("First guest: " + currentGuests.get(0).getGuestName() + 
                               ", Status: " + currentGuests.get(0).getStatus());
        }
        
        // Get count of rooms that need cleaning
        int needsCleaning = (int) roomService.getAllRooms().stream()
            .filter(Room::isNeedsCleaning)
            .count();
        
        model.addAttribute("todayCheckins", todayCheckins);
        model.addAttribute("todayCheckouts", todayCheckouts);
        model.addAttribute("currentGuests", currentGuests);
        model.addAttribute("needsCleaning", needsCleaning);
        model.addAttribute("today", today);
        
        return "checkin-checkout";
    }
    
    @GetMapping("/search")
    public String searchBookings(
            @RequestParam(required = false) String guestName,
            @RequestParam(required = false) BookingStatus status,
            Model model, HttpSession session) {
        
        if (session.getAttribute("staff") == null) {
            return "redirect:/login";
        }
        
        LocalDate today = LocalDate.now();
        
        // Get all bookings
        List<Booking> allBookings = bookingService.getAllBookings();
        
        // Filter based on search criteria
        List<Booking> filteredBookings = allBookings.stream()
            .filter(b -> guestName == null || guestName.isEmpty() || 
                    b.getGuestName().toLowerCase().contains(guestName.toLowerCase()))
            .filter(b -> status == null || b.getStatus() == status)
            .collect(Collectors.toList());
        
        // Prepare data for the different sections
        List<Booking> todayCheckins = filteredBookings.stream()
            .filter(b -> b.getCheckInDate().equals(today))
            .filter(b -> b.getStatus() == BookingStatus.RESERVED || b.getStatus() == BookingStatus.CHECKED_IN)
            .collect(Collectors.toList());
        
        List<Booking> todayCheckouts = filteredBookings.stream()
            .filter(b -> b.getCheckOutDate().equals(today))
            .filter(b -> b.getStatus() == BookingStatus.CHECKED_IN || b.getStatus() == BookingStatus.CHECKED_OUT)
            .collect(Collectors.toList());
        
        List<Booking> currentGuests = filteredBookings.stream()
            .filter(b -> b.getStatus() == BookingStatus.CHECKED_IN)
            .collect(Collectors.toList());
        
        // Get count of rooms that need cleaning
        int needsCleaning = (int) roomService.getAllRooms().stream()
            .filter(Room::isNeedsCleaning)
            .count();
        
        model.addAttribute("todayCheckins", todayCheckins);
        model.addAttribute("todayCheckouts", todayCheckouts);
        model.addAttribute("currentGuests", currentGuests);
        model.addAttribute("needsCleaning", needsCleaning);
        model.addAttribute("today", today);
        model.addAttribute("searchName", guestName);
        model.addAttribute("searchStatus", status);
        
        return "checkin-checkout";
    }
    
    @PostMapping("/bulk-status-change")
    public String bulkStatusChange(
            @RequestParam String roomNumber,
            @RequestParam BookingStatus newStatus,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        
        if (session.getAttribute("staff") == null) {
            return "redirect:/login";
        }
        
        try {
            // Find the room by number
            Optional<Room> roomOptional = roomService.getRoomByNumber(roomNumber);
            
            if (roomOptional.isPresent()) {
                Room room = roomOptional.get();
                
                // Find active bookings for this room (RESERVED or CHECKED_IN)
                List<Booking> activeBookings = bookingService.getBookingsByRoom(room)
                    .stream()
                    .filter(b -> b.getStatus() == BookingStatus.RESERVED || b.getStatus() == BookingStatus.CHECKED_IN)
                    .collect(Collectors.toList());
                
                if (activeBookings.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "No active bookings found for Room " + roomNumber);
                    return "redirect:/checkin-checkout";
                }
                
                // For simplicity, we'll change the status of the first active booking
                Booking bookingToUpdate = activeBookings.get(0);
                
                if (newStatus == BookingStatus.CHECKED_IN) {
                    bookingService.checkIn(bookingToUpdate.getId());
                    redirectAttributes.addFlashAttribute("success", "Guest checked in to Room " + roomNumber + " successfully");
                } else if (newStatus == BookingStatus.CHECKED_OUT) {
                    bookingService.checkOut(bookingToUpdate.getId());
                    redirectAttributes.addFlashAttribute("success", "Guest checked out from Room " + roomNumber + " successfully");
                } else if (newStatus == BookingStatus.RESERVED) {
                    // If currently checked in, revert to reserved
                    if (bookingToUpdate.getStatus() == BookingStatus.CHECKED_IN) {
                        bookingToUpdate.setStatus(BookingStatus.RESERVED);
                        room.setAvailable(false);
                        roomService.updateRoom(room);
                        bookingService.updateBooking(bookingToUpdate);
                        redirectAttributes.addFlashAttribute("success", "Booking for Room " + roomNumber + " reverted to Reserved status");
                    } else {
                        redirectAttributes.addFlashAttribute("error", "Cannot change status to Reserved for this booking");
                    }
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Room " + roomNumber + " not found");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating booking status: " + e.getMessage());
        }
        
        return "redirect:/checkin-checkout";
    }
}
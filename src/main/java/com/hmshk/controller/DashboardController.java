package com.hmshk.controller;

import com.hmshk.model.Booking;
import com.hmshk.model.Room;
import com.hmshk.model.Facility;
import com.hmshk.service.BookingService;
import com.hmshk.service.RoomService;
import com.hmshk.service.FacilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class DashboardController {
    
    private final BookingService bookingService;
    private final RoomService roomService;
    private final FacilityService facilityService;
    


    
    @GetMapping("/")
    public String index(HttpSession session) {
        if (session.getAttribute("staff") != null) {
            return "redirect:/dashboard";
        }
        return "redirect:/login";
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        if (session.getAttribute("staff") == null) {
            return "redirect:/login";
        }
        
        // Always fetch fresh data from database for all dashboard components
        
        // Room occupancy data
        List<Room> allRooms = roomService.getAllRooms();
        List<Room> availableRooms = roomService.getAvailableRooms();
        
        // Today's operations
        LocalDate today = LocalDate.now();
        List<Booking> todayCheckouts = bookingService.getTodayCheckouts();
        List<Booking> todayCheckins = bookingService.getBookingsByDateRange(today, today);
        
        // Calendar data - fetch bookings for the next 30 days
        LocalDate endDate = today.plusDays(30);
        List<Booking> upcomingBookings = bookingService.getBookingsByDateRange(today, endDate);
        
        // Create calendar events for JavaScript
        List<Map<String, Object>> calendarEvents = upcomingBookings.stream()
            .map(booking -> {
                Map<String, Object> event = new HashMap<>();
                event.put("id", booking.getId());
                event.put("title", booking.getGuestName() + " - Room " + booking.getRoom().getRoomNumber());
                event.put("start", booking.getCheckInDate().toString());
                event.put("end", booking.getCheckOutDate().toString());
                
                // Set color based on booking status
                String color;
                switch(booking.getStatus()) {
                    case RESERVED:
                        color = "#fd7e14"; // orange
                        break;
                    case CHECKED_IN:
                        color = "#dc3545"; // red
                        break;
                    case CHECKED_OUT:
                        color = "#198754"; // green
                        break;
                    default:
                        color = "#6c757d"; // gray
                }
                event.put("backgroundColor", color);
                
                return event;
            })
            .collect(Collectors.toList());
        
        // Facility data
        List<Facility> facilities = facilityService.getAllFacilities();
        
        // Room status breakdown
        int occupiedRooms = allRooms.size() - availableRooms.size();
        int needsCleaningCount = (int) allRooms.stream()
            .filter(Room::isNeedsCleaning)
            .count();
        
        // Add all data to model
        model.addAttribute("totalRooms", allRooms.size());
        model.addAttribute("availableRooms", availableRooms.size());
        model.addAttribute("occupiedRooms", occupiedRooms);
        model.addAttribute("needsCleaningCount", needsCleaningCount);
        model.addAttribute("todayCheckouts", todayCheckouts.size());
        model.addAttribute("todayCheckins", todayCheckins.size());
        model.addAttribute("upcomingBookings", upcomingBookings);
        model.addAttribute("calendarEvents", calendarEvents);
        model.addAttribute("rooms", allRooms);
        model.addAttribute("facilities", facilities);
        model.addAttribute("today", today);
        
        return "dashboard";
    }
    
 // Add this to DashboardController.java
    @GetMapping("/api/dashboard/data")
    @ResponseBody
    public Map<String, Object> getDashboardData() {
        Map<String, Object> dashboardData = new HashMap<>();
        
        // Room occupancy data
        List<Room> allRooms = roomService.getAllRooms();
        List<Room> availableRooms = roomService.getAvailableRooms();
        
        // Today's operations
        LocalDate today = LocalDate.now();
        List<Booking> todayCheckouts = bookingService.getTodayCheckouts();
        List<Booking> todayCheckins = bookingService.getBookingsByDateRange(today, today);
        
        // Calendar data
        List<Booking> upcomingBookings = bookingService.getBookingsByDateRange(today, today.plusDays(30));
        List<Map<String, Object>> calendarEvents = createCalendarEvents(upcomingBookings);
        
        // Room status data
        int occupiedRooms = allRooms.size() - availableRooms.size();
        int needsCleaningCount = (int) allRooms.stream().filter(Room::isNeedsCleaning).count();
        
        // Add data to response
        dashboardData.put("totalRooms", allRooms.size());
        dashboardData.put("availableRooms", availableRooms.size());
        dashboardData.put("occupiedRooms", occupiedRooms);
        dashboardData.put("needsCleaningCount", needsCleaningCount);
        dashboardData.put("todayCheckouts", todayCheckouts.size());
        dashboardData.put("todayCheckins", todayCheckins.size());
        dashboardData.put("calendarEvents", calendarEvents);
        dashboardData.put("rooms", allRooms);
        
        return dashboardData;
    }

    // Helper method to create calendar events
    private List<Map<String, Object>> createCalendarEvents(List<Booking> bookings) {
        return bookings.stream()
            .map(booking -> {
                Map<String, Object> event = new HashMap<>();
                event.put("id", booking.getId());
                event.put("title", booking.getGuestName() + " - Room " + booking.getRoom().getRoomNumber());
                event.put("start", booking.getCheckInDate().toString());
                event.put("end", booking.getCheckOutDate().toString());
                
                // Set color based on booking status
                String color;
                switch(booking.getStatus()) {
                    case RESERVED:
                        color = "#fd7e14"; // orange
                        break;
                    case CHECKED_IN:
                        color = "#dc3545"; // red
                        break;
                    case CHECKED_OUT:
                        color = "#198754"; // green
                        break;
                    default:
                        color = "#6c757d"; // gray
                }
                event.put("backgroundColor", color);
                
                return event;
            })
            .collect(Collectors.toList());
    }
}





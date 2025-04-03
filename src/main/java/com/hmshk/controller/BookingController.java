package com.hmshk.controller;

import com.hmshk.model.Booking;
import com.hmshk.model.BookingStatus;
import com.hmshk.model.Room;
import com.hmshk.model.Staff;
import com.hmshk.service.BookingService;
import com.hmshk.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
public class BookingController {
    
    private final BookingService bookingService;
    private final RoomService roomService;
    

    // 有些debugger suggest不需要的，但絕對要的！不可以刪！
    public BookingController(BookingService bookingService, RoomService roomService) {
        this.bookingService = bookingService;
        this.roomService = roomService;
    }

    
    @GetMapping("/bookings")
    public String bookingsPage(Model model, HttpSession session) {
        if (session.getAttribute("staff") == null) {
            return "redirect:/login";
        }
        
        List<Booking> bookings = bookingService.getAllBookings();
        model.addAttribute("bookings", bookings);
        model.addAttribute("statuses", BookingStatus.values());
        return "booking";
    }
    
    @GetMapping("/bookings/new")
    public String newBookingPage(Model model, HttpSession session) {
        if (session.getAttribute("staff") == null) {
            return "redirect:/login";
        }
        
        List<Room> availableRooms = roomService.getAvailableRooms();
        model.addAttribute("rooms", availableRooms);
        model.addAttribute("booking", new Booking());
        model.addAttribute("today", LocalDate.now());
        return "booking-new";
    }
    
    @PostMapping("/bookings/create")
    public String createBooking(@ModelAttribute Booking booking, 
                               @RequestParam Long roomId,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        try {
            Optional<Room> roomOpt = roomService.getRoomById(roomId);
            if (roomOpt.isPresent()) {
                Room room = roomOpt.get();
                booking.setRoom(room);
                booking.setCreatedBy((Staff) session.getAttribute("staff"));
                bookingService.createBooking(booking);
                redirectAttributes.addFlashAttribute("success", "Booking created successfully");
                return "redirect:/bookings";
            } else {
                redirectAttributes.addFlashAttribute("error", "Room not found");
                return "redirect:/bookings/new";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/bookings/new";
        }
    }
    
    @GetMapping("/bookings/{id}")
    public String getBooking(@PathVariable Long id, Model model, HttpSession session) {
        if (session.getAttribute("staff") == null) {
            return "redirect:/login";
        }
        
        Optional<Booking> booking = bookingService.getBookingById(id);
        if (booking.isPresent()) {
            model.addAttribute("booking", booking.get());
            model.addAttribute("rooms", roomService.getAllRooms());
            model.addAttribute("statuses", BookingStatus.values());
            return "booking-edit";
        } else {
            return "redirect:/bookings";
        }
    }
    
    @PostMapping("/bookings/update")
    public String updateBooking(@ModelAttribute Booking booking,
                               @RequestParam Long roomId,
                               RedirectAttributes redirectAttributes) {
        try {
            Optional<Room> roomOpt = roomService.getRoomById(roomId);
            if (roomOpt.isPresent()) {
                Room room = roomOpt.get();
                booking.setRoom(room);
                bookingService.updateBooking(booking);
                redirectAttributes.addFlashAttribute("success", "Booking updated successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Room not found");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/bookings";
    }
    
    @GetMapping("/bookings/cancel/{id}")
    public String cancelBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelBooking(id);
            redirectAttributes.addFlashAttribute("success", "Booking cancelled successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/bookings";
    }
    
    @GetMapping("/bookings/checkin/{id}")
    public String checkIn(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.checkIn(id);
            redirectAttributes.addFlashAttribute("success", "Guest checked in successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/bookings";
    }
    
    @GetMapping("/bookings/checkout/{id}")
    public String checkOut(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.checkOut(id);
            redirectAttributes.addFlashAttribute("success", "Guest checked out successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/bookings";
    }
    
    @GetMapping("/bookings/filter")
    public String filterBookings(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {
        
        List<Booking> filteredBookings;
        
        if (status != null) {
            filteredBookings = bookingService.getBookingsByStatus(status);
        } else if (startDate != null && endDate != null) {
            filteredBookings = bookingService.getBookingsByDateRange(startDate, endDate);
        } else {
            filteredBookings = bookingService.getAllBookings();
        }
        
        model.addAttribute("bookings", filteredBookings);
        model.addAttribute("statuses", BookingStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        
        return "booking";
    }
    
    @GetMapping("/booking-edit")
    public String showBookingEdit() {
        return "booking-edit"; // This must match the template name exactly
    }
}

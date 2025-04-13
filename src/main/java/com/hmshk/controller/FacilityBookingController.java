package com.hmshk.controller;

import com.hmshk.model.*;
import com.hmshk.service.CustomerService;
import com.hmshk.service.FacilityBookingService;
import com.hmshk.service.FacilityService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/facility-bookings")
public class FacilityBookingController {

    private final FacilityBookingService facilityBookingService;
    private final FacilityService facilityService;
    private final CustomerService customerService;
    
    public FacilityBookingController(FacilityBookingService facilityBookingService,
                                    FacilityService facilityService,
                                    CustomerService customerService) {
        this.facilityBookingService = facilityBookingService;
        this.facilityService = facilityService;
        this.customerService = customerService;
    }
    
    @GetMapping
    public String listBookings(
            @RequestParam(required = false) FacilityBookingStatus status,
            @RequestParam(required = false) Long facilityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model, HttpSession session) {
        
        if (session.getAttribute("staff") == null) {
            return "redirect:/login";
        }
        
        List<FacilityBooking> bookings;
        
        if (status != null) {
            bookings = facilityBookingService.getBookingsByStatus(status);
        } else if (facilityId != null) {
            Optional<Facility> facility = facilityService.getFacilityById(facilityId);
            if (facility.isPresent()) {
                bookings = facilityBookingService.getBookingsByFacility(facility.get());
            } else {
                bookings = facilityBookingService.getAllBookings();
            }
        } else if (date != null) {
            bookings = facilityBookingService.getBookingsByDate(date);
        } else {
            bookings = facilityBookingService.getAllBookings();
        }
        
        model.addAttribute("bookings", bookings);
        model.addAttribute("facilities", facilityService.getAllFacilities());
        model.addAttribute("calendarEvents", facilityBookingService.getCalendarEvents());
        
        return "facility-booking";
    }
    
    @GetMapping("/new")
    public String newBookingForm(
            @RequestParam(required = false) Long facilityId,
            @RequestParam(required = false) Long customerId,
            Model model, HttpSession session) {
        
        if (session.getAttribute("staff") == null) {
            return "redirect:/login";
        }
        
        FacilityBooking bookingRequest = new FacilityBooking();
        
        // Pre-select facility if provided
        if (facilityId != null) {
            Optional<Facility> facility = facilityService.getFacilityById(facilityId);
            facility.ifPresent(bookingRequest::setFacility);
        }
        
        // Pre-select customer if provided
        if (customerId != null) {
            Optional<Customer> customer = customerService.getCustomerById(customerId);
            if (customer.isPresent()) {
                Customer selectedCustomer = customer.get();
                bookingRequest.setCustomer(selectedCustomer);
                bookingRequest.setBookerName(selectedCustomer.getName());
                bookingRequest.setBookerEmail(selectedCustomer.getEmail());
                bookingRequest.setBookerPhone(selectedCustomer.getPhoneNumber());
            }
        }
        
        model.addAttribute("bookingRequest", bookingRequest);
        model.addAttribute("facilities", facilityService.getAllFacilities());
        model.addAttribute("customers", customerService.getActiveCustomers());
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("minDate", LocalDate.now());
        
        return "facility-booking-new";
    }
    
    @PostMapping("/create")
    public String createBooking(
            @ModelAttribute FacilityBooking booking,
            @RequestParam Long facilityId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false, defaultValue = "true") boolean isGuestBooking,
            @RequestParam(required = false, defaultValue = "false") boolean isRequest,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        try {
            if (session.getAttribute("staff") == null) {
                return "redirect:/login";
            }
            
            Staff staff = (Staff) session.getAttribute("staff");
            
            // Set facility
            Optional<Facility> facilityOpt = facilityService.getFacilityById(facilityId);
            if (!facilityOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Facility not found");
                return "redirect:/facility-bookings/new";
            }
            booking.setFacility(facilityOpt.get());
            
            // Set customer if it's a member booking
            if (!isGuestBooking && customerId != null) {
                Optional<Customer> customerOpt = customerService.getCustomerById(customerId);
                if (customerOpt.isPresent()) {
                    booking.setCustomer(customerOpt.get());
                }
            }
            
            // Create booking based on request flag
            FacilityBooking createdBooking;
            if (isRequest) {
                createdBooking = facilityBookingService.createBookingRequest(booking, staff);
                redirectAttributes.addFlashAttribute("success", "Facility booking request submitted successfully. An administrator will review your request.");
            } else {
                createdBooking = facilityBookingService.createBooking(booking, staff);
                redirectAttributes.addFlashAttribute("success", "Facility booking created successfully");
            }
            
            return "redirect:/facility-bookings/" + createdBooking.getId();
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/facility-bookings/new";
        }
    }
    
    @PostMapping("/create-member")
    public String createMemberBooking(
            @ModelAttribute FacilityBooking booking,
            @RequestParam Long facilityId,
            @RequestParam Long customerId,
            @RequestParam(required = false, defaultValue = "false") boolean isRequest,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        try {
            if (session.getAttribute("staff") == null) {
                return "redirect:/login";
            }
            
            Staff staff = (Staff) session.getAttribute("staff");
            
            // Set facility
            Optional<Facility> facilityOpt = facilityService.getFacilityById(facilityId);
            if (!facilityOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Facility not found");
                return "redirect:/facility-bookings/new";
            }
            booking.setFacility(facilityOpt.get());
            
            // Set customer
            Optional<Customer> customerOpt = customerService.getCustomerById(customerId);
            if (!customerOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Customer not found");
                return "redirect:/facility-bookings/new";
            }
            
            Customer customer = customerOpt.get();
            booking.setCustomer(customer);
            
            // Auto-fill customer details if not provided
            if (booking.getBookerName() == null || booking.getBookerName().isEmpty()) {
                booking.setBookerName(customer.getName());
            }
            if (booking.getBookerEmail() == null || booking.getBookerEmail().isEmpty()) {
                booking.setBookerEmail(customer.getEmail());
            }
            if (booking.getBookerPhone() == null || booking.getBookerPhone().isEmpty()) {
                booking.setBookerPhone(customer.getPhoneNumber());
            }
            
            // Create booking based on request flag
            FacilityBooking createdBooking;
            if (isRequest) {
                createdBooking = facilityBookingService.createBookingRequest(booking, staff);
                redirectAttributes.addFlashAttribute("success", "Member facility booking request submitted successfully. An administrator will review your request.");
            } else {
                createdBooking = facilityBookingService.createBooking(booking, staff);
                redirectAttributes.addFlashAttribute("success", "Member facility booking created successfully");
            }
            
            return "redirect:/facility-bookings/" + createdBooking.getId();
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/facility-bookings/new";
        }
    }
    
    @GetMapping("/{id}")
    public String viewBooking(@PathVariable Long id, Model model, HttpSession session) {
        if (session.getAttribute("staff") == null) {
            return "redirect:/login";
        }
        
        Optional<FacilityBooking> bookingOpt = facilityBookingService.getBookingById(id);
        
        if (bookingOpt.isPresent()) {
            model.addAttribute("booking", bookingOpt.get());
            return "facility-booking-details";
        } else {
            return "redirect:/facility-bookings";
        }
    }
    
    @GetMapping("/edit/{id}")
    public String editBookingForm(@PathVariable Long id, Model model, HttpSession session) {
        if (session.getAttribute("staff") == null) {
            return "redirect:/login";
        }
        
        Optional<FacilityBooking> bookingOpt = facilityBookingService.getBookingById(id);
        
        if (bookingOpt.isPresent()) {
            model.addAttribute("bookingRequest", bookingOpt.get());
            model.addAttribute("facilities", facilityService.getAllFacilities());
            model.addAttribute("customers", customerService.getActiveCustomers());
            model.addAttribute("statuses", FacilityBookingStatus.values());
            model.addAttribute("minDate", LocalDate.now());
            return "facility-booking-form";
        } else {
            return "redirect:/facility-bookings";
        }
    }
    
    @PostMapping("/save")
    public String saveBooking(
            @ModelAttribute FacilityBooking booking,
            @RequestParam Long facilityId,
            @RequestParam(required = false) Long customerId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        try {
            if (session.getAttribute("staff") == null) {
                return "redirect:/login";
            }
            
            // Set facility
            Optional<Facility> facilityOpt = facilityService.getFacilityById(facilityId);
            if (!facilityOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Facility not found");
                return "redirect:/facility-bookings/edit/" + booking.getId();
            }
            booking.setFacility(facilityOpt.get());
            
            // Set customer if provided
            if (customerId != null) {
                Optional<Customer> customerOpt = customerService.getCustomerById(customerId);
                customerOpt.ifPresent(booking::setCustomer);
            } else {
                booking.setCustomer(null);
            }
            
            // Update booking
            FacilityBooking updatedBooking = facilityBookingService.updateBooking(booking);
            
            redirectAttributes.addFlashAttribute("success", "Facility booking updated successfully");
            return "redirect:/facility-bookings/" + updatedBooking.getId();
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return booking.getId() != null ?
                    "redirect:/facility-bookings/edit/" + booking.getId() :
                    "redirect:/facility-bookings/new";
        }
    }
    
    @GetMapping("/confirm/{id}")
    public String confirmBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            facilityBookingService.confirmBooking(id);
            redirectAttributes.addFlashAttribute("success", "Booking confirmed successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/facility-bookings/" + id;
    }
    
    @GetMapping("/complete/{id}")
    public String completeBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            facilityBookingService.completeBooking(id);
            redirectAttributes.addFlashAttribute("success", "Booking marked as completed");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/facility-bookings/" + id;
    }
    
    @GetMapping("/cancel/{id}")
    public String cancelBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            facilityBookingService.cancelBooking(id);
            redirectAttributes.addFlashAttribute("success", "Booking cancelled successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/facility-bookings/" + id;
    }
    
    @GetMapping("/by-facility/{facilityId}")
    public String getBookingsByFacility(@PathVariable Long facilityId, Model model, HttpSession session) {
        if (session.getAttribute("staff") == null) {
            return "redirect:/login";
        }
        
        Optional<Facility> facilityOpt = facilityService.getFacilityById(facilityId);
        
        if (facilityOpt.isPresent()) {
            Facility facility = facilityOpt.get();
            List<FacilityBooking> bookings = facilityBookingService.getBookingsByFacility(facility);
            
            model.addAttribute("facility", facility);
            model.addAttribute("bookings", bookings);
            
            return "facility-booking-by-facility";
        } else {
            return "redirect:/facilities";
        }
    }
    
    @GetMapping("/customer")
    public String selectCustomer(Model model, HttpSession session) {
        if (session.getAttribute("staff") == null) {
            return "redirect:/login";
        }
        
        List<Customer> customers = customerService.getActiveCustomers();
        model.addAttribute("customers", customers);
        
        return "facility-booking-customer-select";
    }
    
    @GetMapping("/customer/{customerId}")
    public String getBookingsByCustomer(@PathVariable Long customerId, Model model, HttpSession session) {
        if (session.getAttribute("staff") == null) {
            return "redirect:/login";
        }
        
        Optional<Customer> customerOpt = customerService.getCustomerById(customerId);
        
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            List<FacilityBooking> bookings = facilityBookingService.getBookingsByCustomer(customer);
            
            model.addAttribute("customer", customer);
            model.addAttribute("bookings", bookings);
            
            return "facility-booking-customer";
        } else {
            return "redirect:/customers";
        }
    }
    
    @GetMapping("/check-availability")
    @ResponseBody
    public Map<String, Boolean> checkAvailability(
            @RequestParam Long facilityId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime) {
        
        boolean isAvailable = facilityBookingService.isTimeSlotAvailable(facilityId, date, startTime, endTime);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", isAvailable);
        
        return response;
    }
    
    @GetMapping("/api/facilities/{id}")
    @ResponseBody
    public ResponseEntity<?> getFacilityDetails(@PathVariable Long id) {
        return facilityService.getFacilityById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/api/facility-bookings/{id}")
    @ResponseBody
    public ResponseEntity<?> getBookingDetails(@PathVariable Long id) {
        return facilityBookingService.getBookingById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/facility-requests")
    public String facilityRequestsPage(Model model, HttpSession session) {
        if (session.getAttribute("staff") == null) {
            return "redirect:/login";
        }
        
        List<FacilityBooking> requestedBookings = facilityBookingService.getBookingsByStatus(FacilityBookingStatus.REQUESTED);
        model.addAttribute("bookings", requestedBookings);
        model.addAttribute("facilities", facilityService.getAllFacilities());
        return "facility-booking-requests";
    }

    @PostMapping("/approve/{id}")
    public String approveFacilityBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            facilityBookingService.confirmBooking(id);
            redirectAttributes.addFlashAttribute("success", "Facility booking request approved successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/facility-bookings/facility-requests";
    }

    @PostMapping("/reject/{id}")
    public String rejectFacilityBooking(@PathVariable Long id, @RequestParam(required = false) String reason, 
                               RedirectAttributes redirectAttributes) {
        try {
            facilityBookingService.cancelBooking(id);
            // If you want to store rejection reasons, you'll need to modify the cancelBooking method
            // facilityBookingService.cancelBooking(id, reason);
            redirectAttributes.addFlashAttribute("success", "Facility booking request rejected");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/facility-bookings/facility-requests";
    }
    
 // GET mapping for approving a booking
    @GetMapping("/approve/{id}")
    public String approveBookingGet(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            facilityBookingService.confirmBooking(id);
            redirectAttributes.addFlashAttribute("success", "Facility booking request approved successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/facility-bookings/facility-requests";
    }

    // GET mapping for rejecting a booking
    @GetMapping("/reject/{id}")
    public String rejectBookingGet(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            facilityBookingService.cancelBooking(id);
            redirectAttributes.addFlashAttribute("success", "Facility booking request rejected");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/facility-bookings/facility-requests";
    }

	public FacilityBookingService getFacilityBookingService() {
		return facilityBookingService;
	}

	public FacilityService getFacilityService() {
		return facilityService;
	}

	public CustomerService getCustomerService() {
		return customerService;
	}
    
}
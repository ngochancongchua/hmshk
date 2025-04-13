package com.hmshk.service;

import com.hmshk.model.*;
import com.hmshk.repository.FacilityBookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FacilityBookingService {
    
    private final FacilityBookingRepository facilityBookingRepository;
    private final FacilityService facilityService;
    private final CustomerService customerService;
    
    public FacilityBookingService(FacilityBookingRepository facilityBookingRepository, 
                                 FacilityService facilityService, 
                                 CustomerService customerService) {
        this.facilityBookingRepository = facilityBookingRepository;
        this.facilityService = facilityService;
        this.customerService = customerService;
    }
    
    public List<FacilityBooking> getAllBookings() {
        return facilityBookingRepository.findAll();
    }
    
    public Optional<FacilityBooking> getBookingById(Long id) {
        return facilityBookingRepository.findById(id);
    }
    
    public List<FacilityBooking> getBookingsByFacility(Facility facility) {
        return facilityBookingRepository.findByFacility(facility);
    }
    
    public List<FacilityBooking> getBookingsByCustomer(Customer customer) {
        return facilityBookingRepository.findByCustomer(customer);
    }
    
    public List<FacilityBooking> getBookingsByStatus(FacilityBookingStatus status) {
        return facilityBookingRepository.findByStatus(status);
    }
    
    public List<FacilityBooking> getBookingsByDate(LocalDate date) {
        return facilityBookingRepository.findByBookingDate(date);
    }
    
    public List<FacilityBooking> getBookingsByFacilityAndDate(Long facilityId, LocalDate date) {
        return facilityBookingRepository.findByFacilityAndDate(facilityId, date);
    }
    
    public boolean isTimeSlotAvailable(Long facilityId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        if (startTime.equals(endTime) || startTime.isAfter(endTime)) {
            return false;
        }
        
        List<FacilityBooking> overlappingBookings = facilityBookingRepository.findOverlappingBookings(
                facilityId, date, startTime, endTime);
        
        return overlappingBookings.isEmpty();
    }
    
    @Transactional
    public FacilityBooking createBooking(FacilityBooking booking, Staff staff) {
        // Get facility
        Facility facility = booking.getFacility();
        
        // Check facility availability
        if (!facility.isAvailable()) {
            throw new RuntimeException("Facility is not available for booking");
        }
        
        // Check time slot availability
        if (!isTimeSlotAvailable(facility.getId(), booking.getBookingDate(), 
                booking.getStartTime(), booking.getEndTime())) {
            throw new RuntimeException("The requested time slot is not available");
        }
        
        // Set created by staff
        booking.setCreatedBy(staff);
        
        // Set status to reserved
        booking.setStatus(FacilityBookingStatus.REQUESTED);
        
        // Calculate total cost based on hours (rounded up)
        long minutes = Duration.between(booking.getStartTime(), booking.getEndTime()).toMinutes();
        int hours = (int) Math.ceil(minutes / 60.0);
        BigDecimal hourlyRate = facility.getCostPerHour();
        BigDecimal totalCost = hourlyRate.multiply(BigDecimal.valueOf(hours));
        booking.setTotalCost(totalCost);
        
        return facilityBookingRepository.save(booking);
    }
    
    @Transactional
    public FacilityBooking updateBooking(FacilityBooking booking) {
        FacilityBooking existingBooking = facilityBookingRepository.findById(booking.getId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        // If facility has changed, check its availability
        if (!existingBooking.getFacility().getId().equals(booking.getFacility().getId())) {
            Facility newFacility = booking.getFacility();
            
            if (!newFacility.isAvailable()) {
                throw new RuntimeException("New facility is not available for booking");
            }
        }
        
        // Check time slot availability only if date/time has changed
        boolean timeChanged = !existingBooking.getBookingDate().equals(booking.getBookingDate()) ||
                             !existingBooking.getStartTime().equals(booking.getStartTime()) ||
                             !existingBooking.getEndTime().equals(booking.getEndTime());
        
        if (timeChanged) {
            // Only check against other bookings, not this one
            List<FacilityBooking> overlappingBookings = facilityBookingRepository.findOverlappingBookings(
                    booking.getFacility().getId(), booking.getBookingDate(), 
                    booking.getStartTime(), booking.getEndTime());
            
            boolean hasConflict = overlappingBookings.stream()
                    .anyMatch(b -> !b.getId().equals(existingBooking.getId()));
            
            if (hasConflict) {
                throw new RuntimeException("The requested time slot is not available");
            }
            
            // Recalculate cost if time changed
            long minutes = Duration.between(booking.getStartTime(), booking.getEndTime()).toMinutes();
            int hours = (int) Math.ceil(minutes / 60.0);
            BigDecimal hourlyRate = booking.getFacility().getCostPerHour();
            BigDecimal totalCost = hourlyRate.multiply(BigDecimal.valueOf(hours));
            booking.setTotalCost(totalCost);
        }
        
        return facilityBookingRepository.save(booking);
    }
    
    @Transactional
    public void changeBookingStatus(Long bookingId, FacilityBookingStatus newStatus) {
        FacilityBooking booking = facilityBookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        booking.setStatus(newStatus);
        facilityBookingRepository.save(booking);
    }
    @Transactional
    public FacilityBooking createBookingRequest(FacilityBooking booking, Staff staff) {
        // Get facility
        Facility facility = booking.getFacility();
        
        // Check facility availability
        if (!facility.isAvailable()) {
            throw new RuntimeException("Facility is not available for booking");
        }
        
        // Check time slot availability
        if (!isTimeSlotAvailable(facility.getId(), booking.getBookingDate(), 
                booking.getStartTime(), booking.getEndTime())) {
            throw new RuntimeException("The requested time slot is not available");
        }
        
        // Set created by staff
        booking.setCreatedBy(staff);
        
        // Set status to requested
        booking.setStatus(FacilityBookingStatus.REQUESTED);
        
        // Calculate total cost based on hours (rounded up)
        long minutes = Duration.between(booking.getStartTime(), booking.getEndTime()).toMinutes();
        int hours = (int) Math.ceil(minutes / 60.0);
        BigDecimal hourlyRate = facility.getCostPerHour();
        BigDecimal totalCost = hourlyRate.multiply(BigDecimal.valueOf(hours));
        booking.setTotalCost(totalCost);
        
        return facilityBookingRepository.save(booking);
    }
    
    public FacilityBookingRepository getFacilityBookingRepository() {
		return facilityBookingRepository;
	}

	public FacilityService getFacilityService() {
		return facilityService;
	}

	public CustomerService getCustomerService() {
		return customerService;
	}

	@Transactional
    public void confirmBooking(Long bookingId) {
        changeBookingStatus(bookingId, FacilityBookingStatus.CONFIRMED);
    }
    
    @Transactional
    public void completeBooking(Long bookingId) {
        changeBookingStatus(bookingId, FacilityBookingStatus.COMPLETED);
    }
    
    @Transactional
    public void cancelBooking(Long bookingId) {
        changeBookingStatus(bookingId, FacilityBookingStatus.CANCELLED);
    }
    
    public List<Map<String, Object>> getCalendarEvents() {
        List<FacilityBooking> bookings = facilityBookingRepository.findAll();
        
        return bookings.stream().map(booking -> {
            Map<String, Object> event = new HashMap<>();
            event.put("id", booking.getId());
            event.put("title", booking.getBookerName() + " - " + booking.getFacility().getName());
            event.put("start", booking.getBookingDate() + "T" + booking.getStartTime());
            event.put("end", booking.getBookingDate() + "T" + booking.getEndTime());
            event.put("status", booking.getStatus().toString());
            event.put("facilityId", booking.getFacility().getId());
            
            Map<String, Object> extendedProps = new HashMap<>();
            extendedProps.put("status", booking.getStatus().toString());
            extendedProps.put("facilityId", booking.getFacility().getId());
            extendedProps.put("bookerName", booking.getBookerName());
            event.put("extendedProps", extendedProps);
            
            return event;
        }).collect(Collectors.toList());
    }
    public int countPendingRequests() {
        return facilityBookingRepository.findByStatus(FacilityBookingStatus.REQUESTED).size();
    }
}
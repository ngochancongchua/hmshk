package com.hmshk.service;

import com.hmshk.model.Booking;
import com.hmshk.model.BookingStatus;
import com.hmshk.model.Room;
import com.hmshk.repository.BookingRepository;
import com.hmshk.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {
    
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    
    //add
    public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
    }
    //end
    
    
    // Updated to include logging
    public List<Booking> getBookingsByStatus(BookingStatus status) {
        List<Booking> bookings = bookingRepository.findByStatus(status);
        System.out.println("Found " + bookings.size() + " bookings with status " + status);
        return bookings;
    }
    
    // Rest of the original methods remain unchanged
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }
    
    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }
    
    @Transactional
    public Booking createBooking(Booking booking) {
        // Calculate stay duration and total price
        long days = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
        BigDecimal totalPrice = booking.getRoom().getPricePerNight().multiply(BigDecimal.valueOf(days));
        booking.setTotalPrice(totalPrice);
        
        // Save booking
        Booking savedBooking = bookingRepository.save(booking);
        
        // Update room availability
        Room room = booking.getRoom();
        room.setAvailable(false);
        roomRepository.save(room);
        
        return savedBooking;
    }
    
    @Transactional
    public Booking updateBooking(Booking booking) {
        Optional<Booking> existingBooking = bookingRepository.findById(booking.getId());
        if (existingBooking.isPresent()) {
            Booking original = existingBooking.get();
            
            // Preserve the customer relationship
            if (booking.getCustomer() != null && booking.getCustomer().getId() != null) {
                // Keep the existing customer reference if it's the same ID
                if (original.getCustomer() != null && 
                    original.getCustomer().getId().equals(booking.getCustomer().getId())) {
                    booking.setCustomer(original.getCustomer());
                }
            }
            
            // Recalculate total price if dates changed
            long days = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
            BigDecimal totalPrice = booking.getRoom().getPricePerNight().multiply(BigDecimal.valueOf(days));
            booking.setTotalPrice(totalPrice);
            
            return bookingRepository.save(booking);
        } else {
            throw new RuntimeException("Booking not found");
        }
    }
    
    @Transactional
    public void cancelBooking(Long id) {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
            
            // Make room available again
            Room room = booking.getRoom();
            room.setAvailable(true);
            roomRepository.save(room);
        } else {
            throw new RuntimeException("Booking not found");
        }
    }
    
    @Transactional
    public Booking checkIn(Long bookingId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            booking.setStatus(BookingStatus.CHECKED_IN);
            return bookingRepository.save(booking);
        } else {
            throw new RuntimeException("Booking not found");
        }
    }
    
    @Transactional
    public Booking checkOut(Long bookingId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            booking.setStatus(BookingStatus.CHECKED_OUT);
            bookingRepository.save(booking);
            
            // Make room available and mark for cleaning
            Room room = booking.getRoom();
            room.setAvailable(true);
            room.setNeedsCleaning(true);
            roomRepository.save(room);
            
            return booking;
        } else {
            throw new RuntimeException("Booking not found");
        }
    }
    @Transactional
    public Booking createBookingRequest(Booking booking) {
        // Set status to REQUESTED
        booking.setStatus(BookingStatus.REQUESTED);
        
        // Calculate stay duration and total price
        long days = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
        BigDecimal totalPrice = booking.getRoom().getPricePerNight().multiply(BigDecimal.valueOf(days));
        booking.setTotalPrice(totalPrice);
        
        // Save booking request
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking approveBooking(Long id) {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            
            // Check if the room is still available for these dates
            List<Booking> overlappingBookings = bookingRepository.findByRoom(booking.getRoom())
                .stream()
                .filter(b -> b.getStatus() == BookingStatus.RESERVED || b.getStatus() == BookingStatus.CHECKED_IN)
                .filter(b -> !b.getId().equals(booking.getId()))
                .filter(b -> 
                    (booking.getCheckInDate().isBefore(b.getCheckOutDate()) || booking.getCheckInDate().isEqual(b.getCheckOutDate())) && 
                    (booking.getCheckOutDate().isAfter(b.getCheckInDate()) || booking.getCheckOutDate().isEqual(b.getCheckInDate())))
                .collect(Collectors.toList());
            
            if (!overlappingBookings.isEmpty()) {
                throw new RuntimeException("Room is no longer available for these dates");
            }
            
            // Change status to RESERVED
            booking.setStatus(BookingStatus.RESERVED);
            
            // Update room availability
            Room room = booking.getRoom();
            room.setAvailable(false);
            roomRepository.save(room);
            
            return bookingRepository.save(booking);
        } else {
            throw new RuntimeException("Booking not found");
        }
    }

    @Transactional
    public void rejectBooking(Long id, String reason) {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            booking.setStatus(BookingStatus.CANCELLED);
            
            // If we want to store rejection reason, we'd need to add a field to the Booking entity
            // booking.setRejectionReason(reason);
            
            bookingRepository.save(booking);
        } else {
            throw new RuntimeException("Booking not found");
        }
    }
    
    public List<Booking> getBookingsByDateRange(LocalDate start, LocalDate end) {
        return bookingRepository.findByCheckInDateBetween(start, end);
    }
    
    public List<Booking> getTodayCheckouts() {
        return bookingRepository.findByCheckOutDate(LocalDate.now());
    }
    
    public List<Booking> getBookingsByRoom(Room room) {
        return bookingRepository.findByRoom(room);
    }
    public int countPendingRequests() {
        return bookingRepository.findByStatus(BookingStatus.REQUESTED).size();
    }
}
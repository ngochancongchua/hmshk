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
    
    public List<Booking> getBookingsByStatus(BookingStatus status) {
        return bookingRepository.findByStatus(status);
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
}

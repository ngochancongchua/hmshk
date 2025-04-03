package com.hmshk.repository;

import com.hmshk.model.Booking;
import com.hmshk.model.BookingStatus;
import com.hmshk.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByGuestEmail(String guestEmail);
    List<Booking> findByStatus(BookingStatus status);
    List<Booking> findByRoom(Room room);
    List<Booking> findByCheckInDateBetween(LocalDate start, LocalDate end);
    List<Booking> findByCheckOutDate(LocalDate checkOutDate);
}
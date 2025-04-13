package com.hmshk.repository;

import com.hmshk.model.Customer;
import com.hmshk.model.Facility;
import com.hmshk.model.FacilityBooking;
import com.hmshk.model.FacilityBookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface FacilityBookingRepository extends JpaRepository<FacilityBooking, Long> {
    List<FacilityBooking> findByFacility(Facility facility);
    
    List<FacilityBooking> findByCustomer(Customer customer);
    
    List<FacilityBooking> findByStatus(FacilityBookingStatus status);
    
    List<FacilityBooking> findByBookingDate(LocalDate bookingDate);
    
    @Query("SELECT b FROM FacilityBooking b WHERE b.facility.id = ?1 AND b.bookingDate = ?2 AND " +
           "((b.startTime <= ?4 AND b.endTime >= ?3) OR (b.startTime >= ?3 AND b.startTime < ?4)) AND " +
           "b.status IN ('RESERVED', 'CONFIRMED')")
    List<FacilityBooking> findOverlappingBookings(Long facilityId, LocalDate bookingDate, LocalTime startTime, LocalTime endTime);
    
    @Query("SELECT b FROM FacilityBooking b WHERE b.facility.id = ?1 AND b.bookingDate = ?2")
    List<FacilityBooking> findByFacilityAndDate(Long facilityId, LocalDate bookingDate);
}
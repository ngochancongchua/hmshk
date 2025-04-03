package com.hmshk.repository;

import com.hmshk.model.Room;
import com.hmshk.model.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByRoomNumber(String roomNumber);
    List<Room> findByRoomType(RoomType roomType);
    List<Room> findByAvailable(boolean available);
    
    @Query("SELECT r FROM Room r WHERE r.id NOT IN " +
           "(SELECT b.room.id FROM Booking b WHERE " +
           "((b.checkInDate <= ?2 AND b.checkOutDate >= ?1) OR " +
           "(b.checkInDate >= ?1 AND b.checkInDate < ?2)) AND " +
           "b.status IN ('RESERVED', 'CHECKED_IN'))")
    List<Room> findAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate);
}
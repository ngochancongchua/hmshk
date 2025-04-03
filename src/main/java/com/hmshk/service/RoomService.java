package com.hmshk.service;

import com.hmshk.model.Room;
import com.hmshk.model.RoomType;
import com.hmshk.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomService {
    
    private final RoomRepository roomRepository;
    
    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }
    
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }
    
    public Optional<Room> getRoomById(Long id) {
        return roomRepository.findById(id);
    }
    
    public Optional<Room> getRoomByNumber(String roomNumber) {
        return roomRepository.findByRoomNumber(roomNumber);
    }
    
    public Room addRoom(Room room) {
        Optional<Room> existingRoom = roomRepository.findByRoomNumber(room.getRoomNumber());
        if (existingRoom.isPresent()) {
            throw new RuntimeException("Room number already exists");
        }
        return roomRepository.save(room);
    }
    
    public Room updateRoom(Room room) {
        Optional<Room> existingRoom = roomRepository.findById(room.getId());
        if (existingRoom.isPresent()) {
            return roomRepository.save(room);
        } else {
            throw new RuntimeException("Room not found");
        }
    }
    
    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }
    
    public List<Room> getAvailableRooms() {
        return roomRepository.findByAvailable(true);
    }
    
    public List<Room> getRoomsByType(RoomType roomType) {
        return roomRepository.findByRoomType(roomType);
    }
    
    public List<Room> getAvailableRoomsForDates(LocalDate checkIn, LocalDate checkOut) {
        return roomRepository.findAvailableRooms(checkIn, checkOut);
    }
    
    public void setRoomAvailability(Long roomId, boolean isAvailable) {
        Optional<Room> roomOpt = roomRepository.findById(roomId);
        if (roomOpt.isPresent()) {
            Room room = roomOpt.get();
            room.setAvailable(isAvailable);
            roomRepository.save(room);
        } else {
            throw new RuntimeException("Room not found");
        }
    }
    
    public void setRoomCleaning(Long roomId, boolean needsCleaning) {
        Optional<Room> roomOpt = roomRepository.findById(roomId);
        if (roomOpt.isPresent()) {
            Room room = roomOpt.get();
            room.setNeedsCleaning(needsCleaning);
            roomRepository.save(room);
        } else {
            throw new RuntimeException("Room not found");
        }
    }
}

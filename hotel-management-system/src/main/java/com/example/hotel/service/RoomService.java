package com.example.hotel.service;

import com.example.hotel.entity.Room;
import java.time.LocalDate;
import java.util.List;

public interface RoomService {
    List<Room> getAllRooms();
    
    Room getRoomById(Long id);
    
    Room createRoom(Room room);
    
    Room updateRoom(Long id, Room room);
    
    void deleteRoom(Long id);
    
    Room updateRoomStatus(Long id, String status);
    
    List<Room> getAvailableRoomsByType(String roomType, LocalDate checkInDate, LocalDate checkOutDate);
    
    boolean hasBookings(Long roomId);
} 
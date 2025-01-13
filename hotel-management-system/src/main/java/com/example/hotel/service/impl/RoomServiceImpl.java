package com.example.hotel.service.impl;

import com.example.hotel.entity.Room;
import com.example.hotel.exception.ResourceNotFoundException;
import com.example.hotel.repository.BookingRepository;
import com.example.hotel.repository.RoomRepository;
import com.example.hotel.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {
    @Autowired
    private RoomRepository roomRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Override
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }
    
    @Override
    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("房间不存在"));
    }
    
    @Override
    public Room createRoom(Room room) {
        return roomRepository.save(room);
    }
    
    @Override
    public Room updateRoom(Long id, Room room) {
        Room existingRoom = getRoomById(id);
        
        existingRoom.setRoomNumber(room.getRoomNumber());
        existingRoom.setType(room.getType());
        existingRoom.setPrice(room.getPrice());
        existingRoom.setStatus(room.getStatus());
        
        return roomRepository.save(existingRoom);
    }
    
    @Override
    @Transactional
    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("房间不存在"));
        
        // 删除关联的预订记录
        bookingRepository.deleteByRoomId(id);
        
        // 删除房间
        roomRepository.delete(room);
    }
    
    @Override
    public Room updateRoomStatus(Long id, String status) {
        Room room = getRoomById(id);
        room.setStatus(status);
        return roomRepository.save(room);
    }
    
    @Override
    public List<Room> getAvailableRoomsByType(String roomType, LocalDate checkInDate, LocalDate checkOutDate) {
        // 获取指定类型的所有房间
        List<Room> roomsByType = roomRepository.findByType(roomType);
        
        // 获取指定日期范围内已预订的房间ID列表
        List<Long> bookedRoomIds = bookingRepository.findBookedRoomIds(checkInDate, checkOutDate);
        
        // 过滤出未被预订且状态为空闲的房间
        return roomsByType.stream()
                .filter(room -> !bookedRoomIds.contains(room.getId()))
                .filter(room -> "空闲".equals(room.getStatus()))
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean hasBookings(Long roomId) {
        return bookingRepository.existsByRoomId(roomId);
    }
} 
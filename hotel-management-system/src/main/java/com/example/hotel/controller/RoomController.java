package com.example.hotel.controller;

import com.example.hotel.dto.ApiResponse;
import com.example.hotel.entity.Room;
import com.example.hotel.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import java.util.HashMap;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    @Autowired
    private RoomService roomService;
    
    @GetMapping
    public ApiResponse<?> getAllRooms(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {
        try {
            List<Room> rooms = roomService.getAllRooms();
            
            // 如果有状态参数，进行过滤
            if (status != null && !status.isEmpty()) {
                rooms = rooms.stream()
                    .filter(room -> status.equals(room.getStatus()))
                    .collect(Collectors.toList());
            }
            
            // 如果有类型参数，进行过滤
            if (type != null && !type.isEmpty()) {
                rooms = rooms.stream()
                    .filter(room -> type.equals(room.getType()))
                    .collect(Collectors.toList());
            }
            
            return ApiResponse.success(rooms);
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ApiResponse<?> getRoomById(@PathVariable Long id) {
        try {
            return ApiResponse.success(roomService.getRoomById(id));
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    @PostMapping
    public ApiResponse<?> createRoom(@RequestBody Room room) {
        try {
            return ApiResponse.success(roomService.createRoom(room));
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ApiResponse<?> updateRoom(@PathVariable Long id, @RequestBody Room room) {
        try {
            return ApiResponse.success(roomService.updateRoom(id, room));
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteRoom(@PathVariable Long id) {
        try {
            roomService.deleteRoom(id);
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    @PatchMapping("/{id}/status")
    public ApiResponse<?> updateRoomStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            return ApiResponse.success(roomService.updateRoomStatus(id, status));
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    @GetMapping("/available")
    public ApiResponse<?> getAvailableRoomsByType(
        @RequestParam String roomType,
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkInDate,
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkOutDate) {
        try {
            List<Room> availableRooms = roomService.getAvailableRoomsByType(roomType, checkInDate, checkOutDate);
            return ApiResponse.success(availableRooms);
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    @GetMapping("/{id}/check-bookings")
    @Operation(summary = "检查房间是否有预订记录")
    public ResponseEntity<?> checkBookings(@PathVariable Long id) {
        boolean hasBookings = roomService.hasBookings(id);
        Map<String, Boolean> response = new HashMap<>();
        response.put("hasBookings", hasBookings);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/available-for-booking")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> getAvailableRoomsForBooking() {
        try {
            List<Room> rooms = roomService.getAllRooms();
            // 只返回状态为"空闲"的房间
            List<Room> availableRooms = rooms.stream()
                .filter(room -> "空闲".equals(room.getStatus()))
                .collect(Collectors.toList());
            return ApiResponse.success(availableRooms);
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
} 
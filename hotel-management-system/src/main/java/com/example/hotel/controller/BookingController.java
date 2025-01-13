package com.example.hotel.controller;

import com.example.hotel.dto.ApiResponse;
import com.example.hotel.entity.Booking;
import com.example.hotel.entity.Room;
import com.example.hotel.entity.User;
import com.example.hotel.service.BookingService;
import com.example.hotel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public ApiResponse<?> getBookings(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String keyword) {
        try {
            List<Booking> bookings = bookingService.getBookings(status, startDate, endDate, keyword);
            return ApiResponse.success(bookings);
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ApiResponse<?> getBooking(@PathVariable Long id) {
        try {
            return ApiResponse.success(bookingService.getBooking(id));
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    @PostMapping
    public ApiResponse<?> createBooking(@RequestBody Booking booking) {
        try {
            return ApiResponse.success(bookingService.createBooking(booking));
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ApiResponse<?> updateBooking(@PathVariable Long id, @RequestBody Booking booking) {
        try {
            return ApiResponse.success(bookingService.updateBooking(id, booking));
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    @PatchMapping("/{id}/confirm")
    public ApiResponse<?> confirmBooking(@PathVariable Long id) {
        try {
            return ApiResponse.success(bookingService.confirmBooking(id));
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    @PatchMapping("/{id}/cancel")
    public ApiResponse<?> cancelBooking(@PathVariable Long id) {
        try {
            return ApiResponse.success(bookingService.cancelBooking(id));
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    @PatchMapping("/{id}/check-in")
    public ApiResponse<?> checkIn(@PathVariable Long id) {
        try {
            return ApiResponse.success(bookingService.checkIn(id));
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    @GetMapping("/room-available")
    public ApiResponse<?> checkRoomAvailability(
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkInDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkOutDate) {
        try {
            boolean isAvailable = bookingService.isRoomAvailable(roomId, checkInDate, checkOutDate);
            return ApiResponse.success(isAvailable);
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    @GetMapping("/available-rooms")
    public ApiResponse<?> getAvailableRooms(
            @RequestParam String roomType,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkInDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkOutDate) {
        try {
            List<Room> availableRooms = bookingService.getAvailableRoomsByType(roomType, checkInDate, checkOutDate);
            return ApiResponse.success(availableRooms);
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> getMyBookings() {
        try {
            User currentUser = userService.getCurrentUser();
            return ApiResponse.success(bookingService.getBookingsByUser(currentUser.getId()));
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    @PostMapping("/user-booking")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> createUserBooking(@RequestBody Booking booking) {
        try {
            // 设置当前用户
            User currentUser = userService.getCurrentUser();
            booking.setUser(currentUser);
            
            // 设置初始状态为待确认
            booking.setStatus("pending");
            
            // 创建预订
            Booking savedBooking = bookingService.createBooking(booking);
            return ApiResponse.success(savedBooking);
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
} 
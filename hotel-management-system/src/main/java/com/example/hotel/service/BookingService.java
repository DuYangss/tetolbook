package com.example.hotel.service;

import com.example.hotel.entity.Booking;
import com.example.hotel.entity.Room;
import java.time.LocalDate;
import java.util.List;

public interface BookingService {
    
    // 创建预订
    Booking createBooking(Booking booking);
    
    // 更新预订
    Booking updateBooking(Long id, Booking booking);
    
    // 获取预订详情
    Booking getBooking(Long id);
    
    // 获取预订列表
    List<Booking> getBookings(String status, LocalDate startDate, LocalDate endDate, String keyword);
    
    // 确认预订
    Booking confirmBooking(Long id);
    
    // 取消预订
    Booking cancelBooking(Long id);
    
    // 办理入住
    Booking checkIn(Long id);
    
    // 检查房间可用性
    boolean isRoomAvailable(Long roomId, LocalDate checkInDate, LocalDate checkOutDate);
    
    // 获取指定类型的可用房间
    List<Room> getAvailableRoomsByType(String roomType, LocalDate checkInDate, LocalDate checkOutDate);
    
    List<Booking> getBookingsByUser(Long userId);
} 
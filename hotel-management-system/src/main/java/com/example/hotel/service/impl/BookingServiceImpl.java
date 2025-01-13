package com.example.hotel.service.impl;

import com.example.hotel.entity.Booking;
import com.example.hotel.entity.Room;
import com.example.hotel.repository.BookingRepository;
import com.example.hotel.repository.RoomRepository;
import com.example.hotel.service.BookingService;
import com.example.hotel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public Booking createBooking(Booking booking) {
        // 设置当前用户
        booking.setUser(userService.getCurrentUser());
        
        // 生成预订号
        String bookingNumber = generateBookingNumber();
        booking.setBookingNumber(bookingNumber);
        
        // 设置初始状态
        booking.setStatus("pending");
        
        // 设置创建和更新时间
        LocalDate now = LocalDate.now();
        booking.setCreateTime(now);
        booking.setUpdateTime(now);
        
        // 根据房间ID获取房间信息
        Room room = roomRepository.findById(booking.getRoom().getId())
            .orElseThrow(() -> new RuntimeException("房间不存在"));
        
        // 验证房间信息
        if (!room.getType().equals(booking.getRoomType())) {
            throw new RuntimeException("房间类型不匹配");
        }
        
        // 检查房间是否可用
        if (!isRoomAvailable(room.getId(), booking.getCheckInDate(), booking.getCheckOutDate())) {
            throw new RuntimeException("该房间在选定日期已被预订");
        }
        
        // 设置房间信息
        booking.setRoom(room);
        
        // 更新房间状态为已预订
        room.setStatus("已预订");
        roomRepository.save(room);
        
        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public Booking updateBooking(Long id, Booking booking) {
        Booking existingBooking = bookingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("预订不存在"));
        
        // 更新基本信息
        existingBooking.setCustomerName(booking.getCustomerName());
        existingBooking.setPhone(booking.getPhone());
        existingBooking.setIdCard(booking.getIdCard());
        existingBooking.setRoomType(booking.getRoomType());
        existingBooking.setCheckInDate(booking.getCheckInDate());
        existingBooking.setCheckOutDate(booking.getCheckOutDate());
        existingBooking.setRemarks(booking.getRemarks());
        existingBooking.setUpdateTime(LocalDate.now());
        
        return bookingRepository.save(existingBooking);
    }

    @Override
    public Booking getBooking(Long id) {
        return bookingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("预订不存在"));
    }

    @Override
    public List<Booking> getBookings(String status, LocalDate startDate, LocalDate endDate, String keyword) {
        List<Booking> bookings;
        
        if (startDate != null && endDate != null) {
            bookings = bookingRepository.findByDateRange(startDate, endDate);
        } else if (status != null && !status.isEmpty()) {
            bookings = bookingRepository.findByStatus(status);
        } else if (keyword != null && !keyword.isEmpty()) {
            bookings = bookingRepository.findByCustomerNameContaining(keyword);
        } else {
            bookings = bookingRepository.findAll();
        }
        
        return bookings;
    }

    @Override
    @Transactional
    public Booking confirmBooking(Long id) {
        Booking booking = getBooking(id);
        if (!"pending".equals(booking.getStatus())) {
            throw new RuntimeException("只能确认待确认状态的预订");
        }
        
        // 更新预订状态
        booking.setStatus("confirmed");
        booking.setUpdateTime(LocalDate.now());
        
        // 更新房间状态为已预订
        Room room = booking.getRoom();
        room.setStatus("已预订");
        roomRepository.save(room);
        
        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public Booking cancelBooking(Long id) {
        Booking booking = getBooking(id);
        if (!List.of("pending", "confirmed").contains(booking.getStatus())) {
            throw new RuntimeException("只能取消待确认或已确认状态的预订");
        }
        
        // 更新预订状态
        booking.setStatus("cancelled");
        booking.setUpdateTime(LocalDate.now());
        
        // 更新房间状态为空闲
        Room room = booking.getRoom();
        room.setStatus("空闲");
        roomRepository.save(room);
        
        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public Booking checkIn(Long id) {
        Booking booking = getBooking(id);
        if (!"confirmed".equals(booking.getStatus())) {
            throw new RuntimeException("只能为已确认状态的预订办理入住");
        }
        
        // 更新预订状态
        booking.setStatus("checked_in");
        booking.setUpdateTime(LocalDate.now());
        
        // 更新房间状态为已入住
        Room room = booking.getRoom();
        room.setStatus("已入住");
        roomRepository.save(room);
        
        return bookingRepository.save(booking);
    }

    @Override
    public boolean isRoomAvailable(Long roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        return !bookingRepository.isRoomBooked(roomId, checkInDate, checkOutDate);
    }

    @Override
    public List<Room> getAvailableRoomsByType(String roomType, LocalDate checkInDate, LocalDate checkOutDate) {
        // 获取指定日期范围内已预订的房间ID列表
        List<Long> bookedRoomIds = bookingRepository.findBookedRoomIds(checkInDate, checkOutDate);
        
        // 获取指定类型的所有房间
        List<Room> roomsByType = roomRepository.findByType(roomType);
        
        // 过滤出未被预订且状态为空闲的房间
        return roomsByType.stream()
                .filter(room -> !bookedRoomIds.contains(room.getId()))
                .filter(room -> "空闲".equals(room.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> getBookingsByUser(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    // 生成预订号
    private String generateBookingNumber() {
        String prefix = "B";
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = String.format("%04d", (int) (Math.random() * 10000));
        return prefix + date + random;
    }
} 
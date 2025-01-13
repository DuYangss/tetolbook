package com.example.hotel.service.impl;

import com.example.hotel.dto.StatisticsOverviewDTO;
import com.example.hotel.service.StatisticsService;
import com.example.hotel.repository.BookingRepository;
import com.example.hotel.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.math.BigDecimal;
import java.util.*;

@Service
public class StatisticsServiceImpl implements StatisticsService {
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private RoomRepository roomRepository;
    
    @Override
    public StatisticsOverviewDTO getOverview() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        
        StatisticsOverviewDTO overview = new StatisticsOverviewDTO();
        
        // 设置今日预订数
        overview.setTodayBookings(bookingRepository.countByCreatedAtBetween(startOfDay, endOfDay));
        
        // 设置今日收入
        BigDecimal todayIncome = bookingRepository.sumTotalAmountByCheckInDate(LocalDate.now());
        overview.setTodayIncome(todayIncome != null ? todayIncome : BigDecimal.ZERO);
        
        // 设置房间总数和可用房间数
        int totalRooms = roomRepository.countAll();
        int occupiedRooms = bookingRepository.countByStatus("已入住");
        overview.setTotalRooms(totalRooms);
        overview.setAvailableRooms(totalRooms - occupiedRooms);
        
        // 计算入住率
        double occupancyRate = totalRooms > 0 ? 
            (double) occupiedRooms / totalRooms * 100 : 0.0;
        overview.setOccupancyRate(Math.round(occupancyRate * 100.0) / 100.0);
        
        // 设置今日入住和退房数
        overview.setCheckedInToday(bookingRepository.countByCheckInDate(LocalDate.now()));
        overview.setCheckOutToday(bookingRepository.countByCheckOutDate(LocalDate.now()));
        
        return overview;
    }
    
    @Override
    public Map<String, Object> getIncomeStatistics(String timeRange) {
        LocalDate now = LocalDate.now();
        LocalDate startDate;
        
        // 根据时间范围确定起始日期
        switch (timeRange.toLowerCase()) {
            case "week":
                startDate = now.minusWeeks(1);
                break;
            case "month":
                startDate = now.minusMonths(1);
                break;
            case "year":
                startDate = now.minusYears(1);
                break;
            default:
                startDate = now.minusDays(7); // 默认显示最近7天
        }
        
        // 获取收入数据
        List<Map<String, Object>> incomeData = bookingRepository.getIncomeByDateRange(startDate, now);
        
        Map<String, Object> result = new HashMap<>();
        result.put("timeRange", timeRange);
        result.put("data", incomeData);
        
        return result;
    }
    
    @Override
    public Map<String, Object> getRoomTypeStatistics() {
        // 获取各房型的预订统计
        List<Map<String, Object>> roomTypeStats = bookingRepository.getRoomTypeStatistics();
        
        // 计算总预订数用于计算百分比
        int totalBookings = roomTypeStats.stream()
            .mapToInt(stat -> ((Number) stat.get("bookingCount")).intValue())
            .sum();
            
        // 计算各房型的预订百分比
        roomTypeStats.forEach(stat -> {
            int bookingCount = ((Number) stat.get("bookingCount")).intValue();
            double percentage = totalBookings > 0 ? 
                (double) bookingCount / totalBookings * 100 : 0.0;
            stat.put("percentage", Math.round(percentage * 100.0) / 100.0);
        });
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", totalBookings);
        result.put("details", roomTypeStats);
        
        return result;
    }
    
    @Override
    public List<Map<String, Object>> getBookingTrends(LocalDate startDate, LocalDate endDate) {
        return bookingRepository.getBookingTrendsByDateRange(startDate, endDate);
    }
} 
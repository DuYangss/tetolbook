package com.example.hotel.service;

import com.example.hotel.dto.StatisticsOverviewDTO;
import java.time.LocalDate;
import java.util.Map;
import java.util.List;

public interface StatisticsService {
    // 获取统计概览数据
    StatisticsOverviewDTO getOverview();
    
    // 获取收入统计
    Map<String, Object> getIncomeStatistics(String timeRange);
    
    // 获取房型预订统计
    Map<String, Object> getRoomTypeStatistics();
    
    // 获取预订趋势
    List<Map<String, Object>> getBookingTrends(LocalDate startDate, LocalDate endDate);
} 
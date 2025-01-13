package com.example.hotel.controller;

import com.example.hotel.dto.ApiResponse;
import com.example.hotel.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {
    
    @Autowired
    private StatisticsService statisticsService;
    
    @GetMapping("/overview")
    public ApiResponse<?> getOverview() {
        try {
            return ApiResponse.success(statisticsService.getOverview());
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    @GetMapping("/income")
    public ApiResponse<?> getIncomeStatistics(
            @RequestParam(defaultValue = "week") String timeRange) {
        try {
            return ApiResponse.success(statisticsService.getIncomeStatistics(timeRange));
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    @GetMapping("/room-types")
    public ApiResponse<?> getRoomTypeStatistics() {
        try {
            return ApiResponse.success(statisticsService.getRoomTypeStatistics());
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    @GetMapping("/booking-trends")
    public ApiResponse<?> getBookingTrends(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            return ApiResponse.success(statisticsService.getBookingTrends(startDate, endDate));
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
} 
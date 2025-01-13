package com.example.hotel.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class StatisticsOverviewDTO {
    private Integer todayBookings;      // 今日预订数
    private BigDecimal todayIncome;     // 今日收入
    private Double occupancyRate;       // 入住率
    private Integer availableRooms;     // 可用房间数
    private Integer totalRooms;         // 总房间数
    private Integer checkedInToday;     // 今日入住数
    private Integer checkOutToday;      // 今日退房数
} 
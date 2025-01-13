package com.example.hotel.repository;

import com.example.hotel.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByStatus(String status);
    
    List<Booking> findByCustomerNameContaining(String keyword);
    
    @Query("SELECT b.room.id FROM Booking b WHERE " +
           "b.status IN ('pending', 'confirmed', 'checked_in') AND " +
           "((b.checkInDate <= :checkOutDate AND b.checkOutDate >= :checkInDate))")
    List<Long> findBookedRoomIds(
        @Param("checkInDate") LocalDate checkInDate,
        @Param("checkOutDate") LocalDate checkOutDate
    );

    @Query("SELECT b FROM Booking b WHERE b.checkInDate >= :startDate AND b.checkOutDate <= :endDate")
    List<Booking> findByDateRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.room.id = :roomId " +
           "AND b.status IN ('pending', 'confirmed', 'checked_in') " +
           "AND ((b.checkInDate <= :checkOutDate AND b.checkOutDate >= :checkInDate))")
    boolean isRoomBooked(
        @Param("roomId") Long roomId,
        @Param("checkInDate") LocalDate checkInDate,
        @Param("checkOutDate") LocalDate checkOutDate
    );

    boolean existsByRoomId(Long roomId);

    void deleteByRoomId(Long roomId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.createdAt BETWEEN :startTime AND :endTime")
    Integer countByCreatedAtBetween(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    @Query("SELECT SUM(b.totalAmount) FROM Booking b WHERE b.checkInDate = :date")
    BigDecimal sumTotalAmountByCheckInDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = :status")
    Integer countByStatus(@Param("status") String status);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.checkInDate = :date")
    Integer countByCheckInDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.checkOutDate = :date")
    Integer countByCheckOutDate(@Param("date") LocalDate date);

    @Query("SELECT NEW map(DATE(b.checkInDate) as date, SUM(b.totalAmount) as income) " +
           "FROM Booking b " +
           "WHERE b.checkInDate BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(b.checkInDate) " +
           "ORDER BY date")
    List<Map<String, Object>> getIncomeByDateRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT NEW map(b.room.type as roomType, COUNT(b) as bookingCount, " +
           "SUM(b.totalAmount) as totalIncome) " +
           "FROM Booking b " +
           "GROUP BY b.room.type")
    List<Map<String, Object>> getRoomTypeStatistics();

    @Query("SELECT NEW map(DATE(b.checkInDate) as date, COUNT(b) as bookingCount) " +
           "FROM Booking b " +
           "WHERE b.checkInDate BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(b.checkInDate) " +
           "ORDER BY date")
    List<Map<String, Object>> getBookingTrendsByDateRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId")
    List<Booking> findByUserId(@Param("userId") Long userId);
} 
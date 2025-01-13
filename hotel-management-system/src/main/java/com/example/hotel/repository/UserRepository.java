package com.example.hotel.repository;

import com.example.hotel.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    
    boolean existsByUsername(String username);
    
    @Query("SELECT u FROM User u WHERE " +
           "(:role IS NULL OR :role = '' OR u.role = :role) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<User> findByRoleAndKeyword(
        @Param("role") String role,
        @Param("keyword") String keyword,
        Pageable pageable
    );
    
    List<User> findByRole(String role);
    
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.role = 'ADMIN'")
    boolean existsAdminUser();
    
    @Query("SELECT u FROM User u WHERE u.enabled = :enabled")
    List<User> findByEnabled(@Param("enabled") boolean enabled);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByPhone(String phone);
} 
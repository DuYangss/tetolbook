package com.example.hotel.controller;

import com.example.hotel.dto.ApiResponse;
import com.example.hotel.dto.UserDTO;
import com.example.hotel.entity.User;
import com.example.hotel.enums.UserRole;
import com.example.hotel.service.UserManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/management/users")
public class UserManagementController {
    
    @Autowired
    private UserManagementService userManagementService;
    
    @GetMapping
    public ApiResponse<Page<User>> getAllUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            PageRequest pageRequest = PageRequest.of(page, size, sort);
            Page<User> users = userManagementService.getAllUsers(role, keyword, pageRequest);
            return ApiResponse.success(users);
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ApiResponse<User> getUserById(@PathVariable Long id) {
        try {
            User user = userManagementService.getUserById(id);
            return ApiResponse.success(user);
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    @PostMapping
    public ApiResponse<User> createUser(@Valid @RequestBody UserDTO userDTO) {
        try {
            User user = userManagementService.createUser(userDTO);
            return ApiResponse.success(user);
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ApiResponse<User> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO userDTO) {
        try {
            User user = userManagementService.updateUser(id, userDTO);
            return ApiResponse.success(user);
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteUser(@PathVariable Long id) {
        try {
            userManagementService.deleteUser(id);
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    @PatchMapping("/{id}/status")
    public ApiResponse<?> updateUserStatus(
            @PathVariable Long id,
            @RequestParam boolean enabled) {
        try {
            userManagementService.updateUserStatus(id, enabled);
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    @GetMapping("/check-username")
    public ApiResponse<Boolean> checkUsername(@RequestParam String username) {
        try {
            boolean exists = userManagementService.checkUsername(username);
            return ApiResponse.success(exists);
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    @GetMapping("/current")
    public ApiResponse<User> getCurrentUser() {
        try {
            User user = userManagementService.getCurrentUser();
            return ApiResponse.success(user);
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    @GetMapping("/roles")
    public ApiResponse<UserRole[]> getAllRoles() {
        try {
            return ApiResponse.success(UserRole.values());
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
} 
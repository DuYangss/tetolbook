package com.example.hotel.controller;

import com.example.hotel.dto.ApiResponse;
import com.example.hotel.dto.UserDTO;
import com.example.hotel.entity.User;
import com.example.hotel.enums.UserRole;
import com.example.hotel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<User>> searchUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        
        return ApiResponse.success(userService.searchUsers(role, keyword, pageRequest));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "用户不存在"));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<User> createUser(@Valid @RequestBody UserDTO userDTO) {
        try {
            User user = new User();
            user.setUsername(userDTO.getUsername());
            user.setPassword(userDTO.getPassword());
            user.setName(userDTO.getName());
            user.setRole(userDTO.getRole());
            user.setEmail(userDTO.getEmail());
            user.setPhone(userDTO.getPhone());
            user.setAvatar(userDTO.getAvatar());
            user.setEnabled(true);
            
            return ApiResponse.success(userService.createUser(user));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<User> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO userDTO) {
        try {
            User user = new User();
            user.setId(id);
            user.setName(userDTO.getName());
            user.setRole(userDTO.getRole());
            user.setEmail(userDTO.getEmail());
            user.setPhone(userDTO.getPhone());
            user.setAvatar(userDTO.getAvatar());
            if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
                user.setPassword(userDTO.getPassword());
            }
            
            return ApiResponse.success(userService.updateUser(user));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ApiResponse.success(null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
    
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> status) {
        try {
            userService.updateUserStatus(id, status.get("enabled"));
            return ApiResponse.success(null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
    
    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserRole[]> getAllRoles() {
        return ApiResponse.success(UserRole.values());
    }
    
    @GetMapping("/check-username")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Boolean> checkUsername(@RequestParam String username) {
        return ApiResponse.success(userService.findByUsername(username).isPresent());
    }
    
    @GetMapping("/current")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<User> getCurrentUser() {
        return ApiResponse.success(userService.getCurrentUser());
    }
    
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<User> updateProfile(@Valid @RequestBody UserDTO userDTO) {
        try {
            User currentUser = userService.getCurrentUser();
            currentUser.setName(userDTO.getName());
            currentUser.setEmail(userDTO.getEmail());
            currentUser.setPhone(userDTO.getPhone());
            currentUser.setAvatar(userDTO.getAvatar());
            
            return ApiResponse.success(userService.updateUser(currentUser));
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
} 
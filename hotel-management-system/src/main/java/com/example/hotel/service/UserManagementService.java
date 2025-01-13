package com.example.hotel.service;

import com.example.hotel.dto.UserDTO;
import com.example.hotel.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.util.List;

public interface UserManagementService {
    Page<User> getAllUsers(String role, String keyword, PageRequest pageRequest);
    
    User getUserById(Long id);
    
    User createUser(UserDTO userDTO);
    
    User updateUser(Long id, UserDTO userDTO);
    
    void deleteUser(Long id);
    
    void updateUserStatus(Long id, boolean enabled);
    
    boolean checkUsername(String username);
    
    User getCurrentUser();
} 
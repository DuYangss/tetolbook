package com.example.hotel.service.impl;

import com.example.hotel.dto.UserDTO;
import com.example.hotel.entity.User;
import com.example.hotel.repository.UserRepository;
import com.example.hotel.service.UserManagementService;
import com.example.hotel.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import java.util.List;

@Service
public class UserManagementServiceImpl implements UserManagementService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Bean
    public CommandLineRunner initializeAdmin() {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword("admin123"); // 在实际生产环境中应该使用加密密码
                admin.setName("系统管理员");
                admin.setRole("ADMIN");
                admin.setEnabled(true);
                userRepository.save(admin);
                System.out.println("已创建默认管理员用户");
            }
        };
    }
    
    @Override
    public Page<User> getAllUsers(String role, String keyword, PageRequest pageRequest) {
        return userRepository.findByRoleAndKeyword(role, keyword, pageRequest);
    }
    
    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
    }
    
    @Override
    @Transactional
    public User createUser(UserDTO userDTO) {
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }
        
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(userDTO.getPassword());
        user.setName(userDTO.getName());
        user.setRole(userDTO.getRole());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setAvatar(userDTO.getAvatar());
        user.setEnabled(userDTO.isEnabled());
        
        return userRepository.save(user);
    }
    
    @Override
    @Transactional
    public User updateUser(Long id, UserDTO userDTO) {
        User user = getUserById(id);
        
        user.setName(userDTO.getName());
        user.setRole(userDTO.getRole());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setAvatar(userDTO.getAvatar());
        user.setEnabled(userDTO.isEnabled());
        
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            user.setPassword(userDTO.getPassword());
        }
        
        return userRepository.save(user);
    }
    
    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        if ("ADMIN".equals(user.getRole())) {
            throw new IllegalArgumentException("不能删除管理员用户");
        }
        userRepository.delete(user);
    }
    
    @Override
    @Transactional
    public void updateUserStatus(Long id, boolean enabled) {
        User user = getUserById(id);
        if ("ADMIN".equals(user.getRole())) {
            throw new IllegalArgumentException("不能修改管理员用户状态");
        }
        user.setEnabled(enabled);
        userRepository.save(user);
    }
    
    @Override
    public boolean checkUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("用户未登录");
        }
        
        String username = authentication.getName();
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
    }
} 
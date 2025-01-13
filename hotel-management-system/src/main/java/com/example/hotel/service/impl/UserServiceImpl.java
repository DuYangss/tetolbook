package com.example.hotel.service.impl;

import com.example.hotel.entity.User;
import com.example.hotel.repository.UserRepository;
import com.example.hotel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public boolean validatePassword(User user, String password) {
        return user.getPassword().equals(password);
    }

    @Override
    public User authenticateUser(String username, String password) {
        return findByUsername(username)
                .filter(user -> validatePassword(user, password))
                .orElse(null);
    }

    @Override
    public User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("用户未登录");
        }
        
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("用户不存在"));
    }

    @Override
    public Page<User> searchUsers(String role, String keyword, PageRequest pageRequest) {
        return userRepository.findByRoleAndKeyword(role, keyword, pageRequest);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User createUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("用户名已存在");
        }
        return userRepository.save(user);
    }

    @Override
    public User updateUser(User user) {
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        // 更新用户信息
        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());
        existingUser.setRole(user.getRole());
        
        // 如果提供了新密码则更新密码
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(user.getPassword());
        }
        
        return userRepository.save(existingUser);
    }

    @Override
    public void updateUserStatus(Long id, Boolean enabled) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        user.setEnabled(enabled);
        userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        userRepository.delete(user);
    }

    @Override
    public List<User> getEnabledUsers() {
        return userRepository.findByEnabled(true);
    }

    @Override
    public List<User> getDisabledUsers() {
        return userRepository.findByEnabled(false);
    }
} 
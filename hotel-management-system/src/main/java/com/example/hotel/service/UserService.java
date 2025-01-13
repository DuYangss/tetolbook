package com.example.hotel.service;

import com.example.hotel.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.util.Optional;
import java.util.List;

public interface UserService {
    Optional<User> findByUsername(String username);
    boolean validatePassword(User user, String password);
    User authenticateUser(String username, String password);
    User getCurrentUser();
    Page<User> searchUsers(String role, String keyword, PageRequest pageRequest);
    Optional<User> getUserById(Long id);
    User createUser(User user);
    User updateUser(User user);
    void updateUserStatus(Long id, Boolean enabled);
    void deleteUser(Long id);
    List<User> getEnabledUsers();
    List<User> getDisabledUsers();
} 
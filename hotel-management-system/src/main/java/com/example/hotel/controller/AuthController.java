package com.example.hotel.controller;

import com.example.hotel.dto.LoginRequest;
import com.example.hotel.dto.LoginResponse;
import com.example.hotel.entity.User;
import com.example.hotel.service.UserService;
import com.example.hotel.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:8888", allowCredentials = "true")
public class AuthController {
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        System.out.println("收到登录请求: " + loginRequest.getUsername());
        
        return userService.findByUsername(loginRequest.getUsername())
                .filter(user -> userService.validatePassword(user, loginRequest.getPassword()))
                .map(user -> {
                    String token = jwtUtil.generateToken(user.getUsername());
                    
                    LoginResponse response = new LoginResponse();
                    response.setToken(token);
                    response.setUsername(user.getUsername());
                    response.setName(user.getName());
                    response.setRole(user.getRole());
                    
                    return response;
                })
                .orElseThrow(() -> new IllegalArgumentException("用户名或密码错误"));
    }
} 
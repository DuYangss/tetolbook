package com.example.hotel.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String username;
    private String name;
    private String role;
} 
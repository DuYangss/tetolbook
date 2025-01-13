package com.example.hotel.enums;

public enum UserStatus {
    ENABLED("启用"),
    DISABLED("禁用");
    
    private final String displayName;
    
    UserStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
} 
package com.example.hotel.enums;

public enum UserRole {
    ADMIN("管理员"),
    STAFF("员工");
    
    private final String displayName;
    
    UserRole(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
} 
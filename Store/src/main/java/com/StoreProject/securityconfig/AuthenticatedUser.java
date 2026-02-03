package com.StoreProject.securityconfig;


public class AuthenticatedUser {

    private Long userId;
    private String username;
    private String userType;

    public AuthenticatedUser(Long userId, String username, String userType) {
        this.userId = userId;
        this.username = username;
        this.userType = userType;
    }

    // Getters
    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getUserType() {
        return userType;
    }
}
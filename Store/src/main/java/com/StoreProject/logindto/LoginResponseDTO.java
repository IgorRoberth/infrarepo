package com.StoreProject.logindto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoginResponseDTO {
    private String token;
    private String type = "Bearer";
    private String username;
    private String role;
    private String expiresAt;
    private Long userId;
    private String userType;
    
    public LoginResponseDTO() {}
    
    public LoginResponseDTO(String token, String username, String role, LocalDateTime expiresAt, Long userId, String userType) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.expiresAt = expiresAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.userId = userId;
        this.userType = userType;
    }
    
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
}
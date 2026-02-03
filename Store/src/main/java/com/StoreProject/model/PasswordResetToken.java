package com.StoreProject.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

    @Entity
    @Table(
    name = "password_reset_token",
    indexes = {
        @Index(name = "idx_password_reset_token_email", columnList = "email"),
        @Index(name = "idx_password_reset_token_expiry", columnList = "expiry_date")
    }
)
@Data
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String token;

    @Column(nullable = false, length = 150)
    private String email;

    /**
     * CUSTOMER ou SELLER
     * String simples evita FK desnecessária
     */
    @Column(name = "user_type", nullable = false, length = 20)
    private String userType;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(nullable = false)
    private Boolean used = false;

    /* =======================
       LIFECYCLE
       ======================= */

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    /* =======================
       REGRAS DE NEGÓCIO
       ======================= */

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    public boolean isValid() {
        return !Boolean.TRUE.equals(used) && !isExpired();
    }
}

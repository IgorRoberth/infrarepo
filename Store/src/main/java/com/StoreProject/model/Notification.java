package com.StoreProject.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "notifications",
    indexes = {
        @Index(name = "idx_notification_recipient", columnList = "recipient_type, recipient_id"),
        @Index(name = "idx_notification_read", columnList = "is_read")
    }
)
@Data
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * CUSTOMER ou SELLER
     */
    @Column(name = "recipient_type", nullable = false, length = 20)
    private String recipientType;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    /* =======================
       CONTEÚDO DA NOTIFICAÇÃO
       ======================= */

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "notification_type", length = 50)
    private String notificationType;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    /* =======================
       STATUS
       ======================= */

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    /* =======================
       WEBHOOK
       ======================= */

    @Column(name = "webhook_url", length = 500)
    private String webhookUrl;

    @Column(name = "webhook_sent", nullable = false)
    private Boolean webhookSent = false;

    @Column(name = "webhook_attempts", nullable = false)
    private Integer webhookAttempts = 0;

    @Column(name = "webhook_last_attempt")
    private LocalDateTime webhookLastAttempt;

    /* =======================
       AUDITORIA
       ======================= */

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    /* =======================
       MÉTODOS DE CONVENIÊNCIA
       ======================= */

    public void markAsRead() {
        this.read = true;
        this.readAt = LocalDateTime.now();
    }
}
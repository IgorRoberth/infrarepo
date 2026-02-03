package com.StoreProject.model;

import com.StoreProject.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_history")
@Data
public class OrderHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * FK criada SOMENTE aqui
     * Sem cascade para evitar problemas de ordem no PostgreSQL
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 30)
    private OrderStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 30)
    private OrderStatus newStatus;

    /**
     * CUSTOMER, SELLER ou SYSTEM
     */
    @Column(name = "changed_by", nullable = false, length = 20)
    private String changedBy;

    @Column(name = "changed_by_id")
    private Long changedById;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /* =======================
       LIFECYCLE
       ======================= */

    @PrePersist
    protected void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}

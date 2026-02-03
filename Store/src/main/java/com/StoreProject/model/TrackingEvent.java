package com.StoreProject.model;

import com.StoreProject.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "tracking_event")
@Data
public class TrackingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Lado DONO da relação
     * FK criada SOMENTE aqui (mais seguro no Postgres)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tracking_id", nullable = false)
    private Tracking tracking;

    @Column(name = "data_evento", nullable = false)
    private LocalDateTime dataEvento;

    @Column(nullable = false, length = 255)
    private String descricao;

    @Column(length = 150)
    private String localizacao;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private OrderStatus status;

    @PrePersist
    public void prePersist() {
        this.dataEvento = LocalDateTime.now();
    }
}

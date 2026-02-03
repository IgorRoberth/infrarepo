package com.StoreProject.model;

import com.StoreProject.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tracking")
@Data
public class Tracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * IMPORTANTE:
     * - OneToOne no Postgres é mais estável quando o lado "dono"
     *   NÃO tenta criar cascata agressiva
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "codigo_rastreamento", nullable = false, unique = true, length = 50)
    private String codigoRastreamento;

    @Column(length = 100)
    private String transportadora;

    @Column(name = "data_envio")
    private LocalDateTime dataEnvio;

    @Column(name = "data_entrega_prevista")
    private LocalDateTime dataEntregaPrevista;

    @Column(name = "data_entrega_real")
    private LocalDateTime dataEntregaReal;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private OrderStatus status;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    /**
     * CRÍTICO PARA POSTGRES:
     * - REMOVIDO cascade = ALL
     * - evita FK sendo criada antes da tabela existir
     */
    @OneToMany(mappedBy = "tracking", fetch = FetchType.LAZY)
    private List<TrackingEvent> eventos;

    @PrePersist
    public void prePersist() {
        this.atualizadoEm = LocalDateTime.now();
        if (this.codigoRastreamento == null) {
            this.codigoRastreamento = generateTrackingCode();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }

    private String generateTrackingCode() {
        return "TRK-" + System.currentTimeMillis() + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
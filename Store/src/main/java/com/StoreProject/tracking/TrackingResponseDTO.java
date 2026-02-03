package com.StoreProject.tracking;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TrackingResponseDTO {

    private Long id;
    private String codigoRastreamento;
    private String transportadora;
    private LocalDateTime dataEnvio;
    private LocalDateTime dataEntregaPrevista;
    private LocalDateTime dataEntregaReal;
    private String observacoes;

    // Informações do pedido
    private Long orderId;
    private String orderNumber;

    // Eventos de rastreamento
    private List<TrackingEventResponseDTO> eventos;
}
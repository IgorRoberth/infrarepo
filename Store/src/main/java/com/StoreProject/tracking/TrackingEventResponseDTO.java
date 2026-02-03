package com.StoreProject.tracking;

import com.StoreProject.enums.OrderStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TrackingEventResponseDTO {
    private Long id;
    private LocalDateTime dataEvento;
    private String descricao;
    private String localizacao;
    private OrderStatus status;
    

    private Long trackingId;
    private String codigoRastreamento;
    
    // Dados do pedido
    private Long orderId;
    private String orderNumber;
    
    // Dados do cliente
    private Long customerId;
    private String customerName;
    
    // Dados do vendedor
    private Long sellerId;
    private String sellerName;
}
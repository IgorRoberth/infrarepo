package com.StoreProject.orderdto;

import com.StoreProject.enums.OrderStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderPendingResponseDTO {
    private Long id;
    private String orderNumber;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String enderecoEntrega;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    // Customer info
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String observacoes;
    private String codigoRastreamento;
}
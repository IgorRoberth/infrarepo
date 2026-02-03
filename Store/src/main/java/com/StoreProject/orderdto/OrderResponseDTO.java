package com.StoreProject.orderdto;

import com.StoreProject.enums.OrderStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponseDTO {

    private Long id;
    private String orderNumber;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String enderecoEntrega;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    private Long productId;
    private String productName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    
    // Customer info
    private Long customerId;
    private String customerName;
    private String customerEmail;
    
    // Tracking info
    private String observacoes;
    private String codigoRastreamento;
    
    // Items
    private List<OrderItemResponseDTO> items;

    // Permissões para ações
    private boolean canApprove;
    private boolean canManage;
    private boolean canCancel;
}
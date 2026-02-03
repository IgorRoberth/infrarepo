package com.StoreProject.orderdto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemResponseDTO {
    private Long id;
    private Integer quantidade;
    private BigDecimal precoUnitario;
    private BigDecimal subtotal;

    // Dados do produto
    private Long productId;
    private String productNome;
    private String productDescricao;
    private String productImagemUrl;

    // Dados do pedido
    private Long orderId;
    private String orderNumber;

}
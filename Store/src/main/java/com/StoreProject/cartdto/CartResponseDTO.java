package com.StoreProject.cartdto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CartResponseDTO {
    
    private Long id;
    private Long customerId;
    private String customerName;
    private BigDecimal totalAmount;
    private Integer totalItems;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    private List<CartItemResponseDTO> items;
}
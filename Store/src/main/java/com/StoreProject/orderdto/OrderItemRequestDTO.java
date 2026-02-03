package com.StoreProject.orderdto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class OrderItemRequestDTO {
    
    @NotNull(message = "ID do produto é obrigatório")
    private Long productId;
    
    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser pelo menos 1")
    private Integer quantidade;
}
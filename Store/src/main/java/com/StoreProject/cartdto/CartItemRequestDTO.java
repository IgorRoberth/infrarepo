package com.StoreProject.cartdto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CartItemRequestDTO {
    
    @NotNull(message = "ID do produto é obrigatório")
    private Long productId;
    
    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser pelo menos 1")
    @Max(value = 100, message = "Quantidade máxima é 100")
    private Integer quantidade;
}
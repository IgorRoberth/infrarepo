package com.StoreProject.cartdto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CartItemResponseDTO {
    
    private Long id;
    private Long productId;
    private String productNome;
    private String productDescricao;
    private String productImagemUrl;
    private String productMarca;
    private String productCategoria;
    private Integer quantidade;
    private BigDecimal precoUnitario;
    private BigDecimal subtotal;
    private LocalDateTime adicionadoEm;
    private Boolean productDisponivel;
    private Integer productEstoque;
}
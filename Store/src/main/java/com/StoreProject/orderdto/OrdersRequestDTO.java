package com.StoreProject.orderdto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class OrdersRequestDTO {
    
    @NotNull(message = "ID do cliente é obrigatório")
    private Long customerId;
    @NotBlank(message = "Endereço de entrega é obrigatório")
    @Size(max = 500, message = "Endereço deve ter no máximo 500 caracteres")
    private String enderecoEntrega;
    @Size(max = 1000, message = "Observações devem ter no máximo 1000 caracteres")
    private String observacoes;
    @NotEmpty(message = "Lista de itens não pode estar vazia")
    @Valid
    private List<OrderItemRequestDTO> items;
}
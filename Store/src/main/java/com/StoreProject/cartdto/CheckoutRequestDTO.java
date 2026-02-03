package com.StoreProject.cartdto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CheckoutRequestDTO {
    
    @NotBlank(message = "Endereço de entrega é obrigatório")
    @Size(max = 500, message = "Endereço deve ter no máximo 500 caracteres")
    private String enderecoEntrega;
    
    @Size(max = 1000, message = "Observações devem ter no máximo 1000 caracteres")
    private String observacoes;
    
    @NotBlank(message = "Método de pagamento é obrigatório")
    private String metodoPagamento; // "CARTAO", "PIX", "BOLETO"

    private String numeroCartao;
    private String nomeCartao;
    private String validadeCartao;
    private String cvvCartao;
}
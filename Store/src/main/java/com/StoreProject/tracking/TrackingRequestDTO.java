package com.StoreProject.tracking;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TrackingRequestDTO {

    @NotNull(message = "ID do pedido é obrigatório")
    private Long orderId;

    @NotBlank(message = "Transportadora é obrigatória")
    @Size(max = 100, message = "Nome da transportadora deve ter no máximo 100 caracteres")
    private String transportadora;

    private LocalDateTime dataEnvio;
    private LocalDateTime dataEntregaPrevista;

    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    private String observacoes;
}
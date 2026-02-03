package com.StoreProject.tracking;

import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TrackingUpdateDTO {

    @Size(max = 100, message = "Nome da transportadora deve ter no máximo 100 caracteres")
    private String transportadora;

    private LocalDateTime dataEnvio;
    private LocalDateTime dataEntregaPrevista;
    private LocalDateTime dataEntregaReal;

    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    private String observacoes;

    // Todos os campos opcionais para atualização
}
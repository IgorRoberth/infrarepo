package com.StoreProject.tracking;

import com.StoreProject.enums.OrderStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TrackingEventRequestDTO {
    @NotNull
    private Long trackingId;
    
    private LocalDateTime dataEvento = LocalDateTime.now();
    
    @NotNull
    private String descricao;
    
    private String localizacao;
    
    @NotNull
    private OrderStatus status;
}
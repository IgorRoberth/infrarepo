package com.StoreProject.tracking;

import com.StoreProject.enums.OrderStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TrackingEventUpdateDTO {
    private LocalDateTime dataEvento;
    private String descricao;
    private String localizacao;
    private OrderStatus status;
}
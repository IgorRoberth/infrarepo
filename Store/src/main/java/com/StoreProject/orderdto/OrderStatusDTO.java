package com.StoreProject.orderdto;

import com.StoreProject.enums.OrderStatus;
import lombok.Data;

@Data
public class OrderStatusDTO {
    private OrderStatus status;
    private String descricao;

    public OrderStatusDTO() {}

    public OrderStatusDTO(OrderStatus status) {
        this.status = status;
        this.descricao = status.getDescricao();
    }

    public static OrderStatusDTO from(OrderStatus status) {
        return new OrderStatusDTO(status);
    }
}
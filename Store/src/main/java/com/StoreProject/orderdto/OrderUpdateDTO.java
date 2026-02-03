package com.StoreProject.orderdto;

import com.StoreProject.enums.OrderStatus;

import lombok.Data;

@Data
public class OrderUpdateDTO {
    private String enderecoEntrega;
    private String observacoes;
    private OrderStatus status;
}
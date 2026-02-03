package com.StoreProject.orderdto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class OrderItemUpdateDTO {

    @Min(value = 1, message = "Quantidade deve ser pelo menos 1")
    private Integer quantidade;
}
package com.StoreProject.customerdto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusUpdateDTO {

    @NotNull(message = "Status ativo é obrigatório")
    private Boolean ativo;

    public StatusUpdateDTO() {}

    public StatusUpdateDTO(Boolean ativo) {
        this.ativo = ativo;
    }
}
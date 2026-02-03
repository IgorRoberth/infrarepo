package com.StoreProject.customerdto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CustomerResponseDTO {

    private Long id;
    private String name;
    private String username;
    private String email;
    private String phone;
    private String endereco;
    private String cpf;
    private String cep;
    private String cidade;
    private String estado;
    private LocalDateTime createdIn;
    private Boolean ativo;

}

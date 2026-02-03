package com.StoreProject.logindto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDTO {

    private String username;

    @Email(message = "E-mail deve conter um formato válido")
    private String email; // Para Seller

    @NotBlank(message = "Senha é obrigatória")
    private String password;

    private String userType; // "CUSTOMER" ou "SELLER"
}
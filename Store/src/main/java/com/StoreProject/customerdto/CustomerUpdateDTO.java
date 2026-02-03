package com.StoreProject.customerdto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CustomerUpdateDTO {

@Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
private String name;

@Size(min = 3, max = 50, message = "Username deve ter entre 3 e 50 caracteres")
@Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username deve conter apenas letras, números e underscore")
private String username;

@Email(message = "Email deve ter um formato válido")
private String email;

@Size(min = 6, max = 100, message = "Senha deve ter entre 6 e 100 caracteres")
private String password;

@Pattern(regexp = "^\\(?\\d{2}\\)?[\\s-]?\\d{4,5}[\\s-]?\\d{4}$", message = "Telefone deve ter um formato válido")
private String phone;

@Size(max = 200, message = "Endereço deve ter no máximo 200 caracteres")
private String endereco;

@Pattern(regexp = "^\\d{11}$", message = "CPF deve conter exatamente 11 dígitos")
private String cpf;

@Pattern(regexp = "^\\d{5}-?\\d{3}$", message = "CEP deve ter o formato 00000-000")
private String cep;

@Size(max = 100, message = "Cidade deve ter no máximo 100 caracteres")
private String cidade;

@Size(max = 2, message = "Estado deve ter 2 caracteres")
private String estado;

}
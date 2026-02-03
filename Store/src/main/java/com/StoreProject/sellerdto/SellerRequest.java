package com.StoreProject.sellerdto;

import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Table(name = "seller", uniqueConstraints = {
        @UniqueConstraint(columnNames = "cnpj"), 
        @UniqueConstraint(columnNames = "email")
})
public class SellerRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    @Column(nullable = false)
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "E-mail deve conter um formato válido")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, max = 100, message = "Senha deve ter entre 6 e 100 caracteres")
    @Column(nullable = false)
    private String password;

    @Pattern(regexp = "^\\(?\\d{2}\\)?[\\s-]?\\d{4,5}[\\s-]?\\d{4}$", message = "Telefone deve ter um formato válido")
    private String telefone;

    @Size(max = 200, message = "Endereço deve conter no máximo 200 caracteres")
    private String endereco;

    @Pattern(regexp = "^\\d{14}$", message = "CNPJ deve conter exatamente 14 dígitos")
    @Column(nullable = false, unique = true)
    private String cnpj;

    @Size(max = 150, message = "Razão social deve conter no máximo 150 caracteres")
    private String razaoSocial;
    
    @Pattern(regexp = "^\\d{5}-?\\d{3}$", message = "CEP deve conter um formato válido")
    private String cep;
    
    @Size(max = 2, message = "Estado deve conter 2 caracteres")
    private String estado;
    
    @Size(max = 100, message = "Cidade deve conter no máximo 100 caracteres")
    private String cidade;
}
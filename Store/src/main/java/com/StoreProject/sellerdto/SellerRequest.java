package com.StoreProject.sellerdto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SellerRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve conter entre 2 e 100 caracteres")
    @Pattern(
        regexp = "^(?!.*\\b(https?://|www\\.|\\w+\\.(com|br|net|org)\\b)).*$",
        message = "O nome não pode conter links/URLs"
    )
    @Pattern(
        regexp = "^[A-Za-zÀ-ÖØ-öø-ÿ0-9\\s.'’\\-&]+$",
        message = "Nome com formato inválido"
    )
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "E-mail deve conter um formato válido")
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, max = 100, message = "Senha deve ter entre 6 e 100 caracteres")
    private String password;

    @Pattern(
        regexp = "^\\(?\\d{2}\\)?[\\s-]?\\d{4,5}[\\s-]?\\d{4}$",
        message = "Telefone deve ter um formato válido"
    )
    private String telefone;

    @Size(max = 200, message = "Endereço deve conter no máximo 200 caracteres")
    @Pattern(
        regexp = "^(?!.*\\b(https?://|www\\.|\\w+\\.(com|br|net|org)\\b)).*$",
        message = "Endereço não pode conter links/URLs"
    )
    private String endereco;

    @Pattern(regexp = "^\\d{14}$", message = "CNPJ deve conter exatamente 14 dígitos")
    private String cnpj;

    @Size(max = 150, message = "Razão social deve conter no máximo 150 caracteres")
    @Pattern(
        regexp = "^(?!.*\\b(https?://|www\\.|\\w+\\.(com|br|net|org)\\b)).*$",
        message = "Razão social não pode conter links/URLs"
    )
    private String razaoSocial;

    @Pattern(regexp = "^\\d{5}-?\\d{3}$", message = "CEP deve conter um formato válido")
    private String cep;

    @Size(max = 2, message = "Estado deve conter 2 caracteres")
    @Pattern(regexp = "^[A-Za-z]{2}$", message = "Estado deve conter apenas letras (UF)")
    private String estado;

    @Size(max = 100, message = "Cidade deve conter no máximo 100 caracteres")
    @Pattern(
        regexp = "^(?!.*\\b(https?://|www\\.|\\w+\\.(com|br|net|org)\\b)).*$",
        message = "Cidade não pode conter links/URLs"
    )
    @Pattern(
        regexp = "^[A-Za-zÀ-ÖØ-öø-ÿ\\s.'’\\-]+$",
        message = "Cidade contém caracteres inválidos"
    )
    private String cidade;
}

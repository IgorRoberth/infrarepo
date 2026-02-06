package com.StoreProject.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(
    name = "seller",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_seller_cnpj", columnNames = "cnpj"),
        @UniqueConstraint(name = "uk_seller_email", columnNames = "email")
    }
)
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100)
    @Column(nullable = false, length = 100)
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Email
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, max = 100)
    @Column(nullable = false)
    private String password;

    @Pattern(
        regexp = "^\\(?\\d{2}\\)?[\\s-]?\\d{4,5}[\\s-]?\\d{4}$",
        message = "Telefone deve ter um formato válido"
    )
    @Column(length = 20)
    private String telefone;

    @Size(max = 200)
    @Column(length = 200)
    private String endereco;

    @NotBlank
    @Pattern(regexp = "^\\d{14}$", message = "CNPJ deve conter exatamente 14 dígitos")
    @Column(nullable = false, unique = true, length = 14)
    private String cnpj;

    @Size(max = 150)
    @Column(name = "razao_social", length = 150)
    private String razaoSocial;

    @Pattern(regexp = "^\\d{5}-?\\d{3}$", message = "CEP deve conter formato válido")
    @Column(length = 9)
    private String cep;

    @Size(max = 2)
    @Column(name = "estado", length = 2)
    private String estado;

    @Size(max = 100)
    @Column(length = 100)
    private String cidade;

    @Column(name = "user_type", nullable = false, length = 20)
    private String userType = "SELLER";

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @Column(nullable = false)
    private Boolean ativo = true;

    @OneToMany(mappedBy = "seller", fetch = FetchType.LAZY)
    private List<Product> produtos;

    @PrePersist
    public void prePersist() {
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
        if (this.userType == null) this.userType = "SELLER";
    }

    @PreUpdate
    public void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }
}
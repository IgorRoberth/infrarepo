package com.StoreProject.sellerdto;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public class SellerUpdate {

    @Size(min = 2, max = 100, message = "Nome deve conter entre 2 e 100 caracteres")
    private String nome;

    @Email(message = "E-mail deve conter um formato válido")
    private String email;

    @Size(min = 6, max = 100, message = "Senha deve ter entre 6 e 100 caracteres")
    private String password;

    @Pattern(regexp = "^\\(?\\d{2}\\)?[\\s-]?\\d{4,5}[\\s-]?\\d{4}$", message = "Telefone deve ter um formato válido")
    private String telefone;

    @Size(max = 200, message = "Endereço deve conter no máximo 200 caracteres")
    private String endereco;

    @Pattern(regexp = "^\\d{14}$", message = "CNPJ deve conter exatamente 14 dígitos")
    private String cnpj;

    @Size(max = 150, message = "Razão social deve ter no máximo 150 caracteres")
    private String razaoSocial;

    private String cep;
    private String estado;
    private String cidade;

    public SellerUpdate() {}

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getRazaoSocial() { return razaoSocial; }
    public void setRazaoSocial(String razaoSocial) { this.razaoSocial = razaoSocial; }

    public String getCep() {return this.cep;}
    public void setCep(String cep) { this.cep = cep; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

}
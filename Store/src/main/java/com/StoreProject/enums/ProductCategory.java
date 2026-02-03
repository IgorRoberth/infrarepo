package com.StoreProject.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ProductCategory {
    ROUPAS("Roupas e Acessórios"),
    ELETRONICOS("Eletrônicos"),
    ALIMENTOS("Alimentos e Bebidas"),
    CASA_JARDIM("Casa e Jardim"),
    ESPORTES("Esportes e Lazer"),
    LIVROS("Livros e Mídia"),
    BELEZA("Beleza e Cuidados Pessoais"),
    BRINQUEDOS("Brinquedos e Jogos"),
    AUTOMOTIVO("Automotivo"),
    FERRAMENTAS("Ferramentas e Construção"),
    SAUDE("Saúde e Bem-estar"),
    OUTROS("Outros");

    private final String descricao;

    ProductCategory(String descricao) {
        this.descricao = descricao;
    }

    @JsonValue
    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
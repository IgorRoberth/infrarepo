package com.StoreProject.enums;

public enum ProductStatus {
    ATIVO("Ativo - Disponível para venda"),
    INATIVO("Inativo - Fora do catálogo"),
    ESGOTADO("Esgotado - Sem estoque"),
    DESCONTINUADO("Descontinuado - Não será mais vendido"),
    PENDENTE("Pendente - Aguardando aprovação"),
    PROMOCAO("Em Promoção - Preço especial");

    private final String descricao;

    ProductStatus(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
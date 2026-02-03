package com.StoreProject.enums;

public enum OrderStatus {
    PENDENTE("Pendente - Aguardando aprovação do vendedor"),
    APROVADO("Aprovado - Vendedor aprovou o pedido"),
    CANCELADO("Cancelado - Pedido foi cancelado"),
    PROCESSANDO("Processando - Preparando para envio"),
    ENVIADO("Enviado - Em trânsito"),
    EM_TRANSITO("Em Trânsito - A caminho do destino"),
    ENTREGUE("Entregue - Pedido foi entregue"),
    DEVOLVIDO("Devolvido - Produto foi devolvido"),
    REEMBOLSADO("Reembolsado - Valor foi reembolsado"), CONFIRMADO("Confirmado - Pedido foi confirmado");

    private final String descricao;

    OrderStatus(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
    
    public boolean canTransitionTo(OrderStatus newStatus) {
        switch (this) {
            case PENDENTE:
                return newStatus == APROVADO || newStatus == CANCELADO;
            case APROVADO:
                return newStatus == PROCESSANDO || newStatus == CANCELADO;
            case PROCESSANDO:
                return newStatus == ENVIADO || newStatus == CANCELADO;
            case ENVIADO:
                return newStatus == EM_TRANSITO || newStatus == ENTREGUE;
            case EM_TRANSITO:
                return newStatus == ENTREGUE || newStatus == DEVOLVIDO;
            case ENTREGUE:
                return newStatus == DEVOLVIDO;
            case DEVOLVIDO:
                return newStatus == REEMBOLSADO;
            default:
                return false;
        }
    }
}
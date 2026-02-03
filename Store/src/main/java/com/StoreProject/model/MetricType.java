package com.StoreProject.model;

public enum MetricType {

    // Métricas de Vendas
    TOTAL_REVENUE("Receita Total"),
    AVERAGE_ORDER_VALUE("Valor Médio do Pedido"),
    TOTAL_ORDERS("Total de Pedidos"),
    CONVERSION_RATE("Taxa de Conversão"),

    // Métricas de Produtos
    TOTAL_PRODUCTS("Total de Produtos"),
    PRODUCTS_SOLD("Produtos Vendidos"),
    TOP_SELLING_PRODUCT("Produto Mais Vendido"),
    LOW_STOCK_COUNT("Produtos com Estoque Baixo"),

    // Métricas de Clientes
    TOTAL_CUSTOMERS("Total de Clientes"),
    NEW_CUSTOMERS("Novos Clientes"),
    ACTIVE_CUSTOMERS("Clientes Ativos"),
    CUSTOMER_RETENTION_RATE("Taxa de Retenção"),

    // Métricas de Vendedores
    TOTAL_SELLERS("Total de Vendedores"),
    ACTIVE_SELLERS("Vendedores Ativos"),
    TOP_SELLER("Melhor Vendedor"),
    SELLER_PERFORMANCE("Performance do Vendedor"),

    // Métricas de Entrega
    DELIVERY_SUCCESS_RATE("Taxa de Sucesso na Entrega"),
    AVERAGE_DELIVERY_TIME("Tempo Médio de Entrega"),
    PENDING_DELIVERIES("Entregas Pendentes"),
    DELAYED_DELIVERIES("Entregas Atrasadas"),

    // Métricas de Sistema
    SYSTEM_PERFORMANCE("Performance do Sistema"),
    ERROR_RATE("Taxa de Erro"),
    USER_ACTIVITY("Atividade do Usuário"),

    // Métricas Financeiras
    PROFIT_MARGIN("Margem de Lucro"),
    COST_PER_ACQUISITION("Custo por Aquisição"),
    LIFETIME_VALUE("Valor Vitalício do Cliente"),

    // Métricas Customizadas
    CUSTOM_METRIC("Métrica Customizada");

    private final String description;

    MetricType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
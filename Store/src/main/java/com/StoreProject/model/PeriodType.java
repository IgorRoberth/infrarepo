package com.StoreProject.model;

public enum PeriodType {

    HOURLY("Por Hora"),
    DAILY("Diário"),
    WEEKLY("Semanal"),
    MONTHLY("Mensal"),
    QUARTERLY("Trimestral"),
    YEARLY("Anual"),
    CUSTOM("Período Customizado"),
    REAL_TIME("Tempo Real"),
    ALL_TIME("Todo o Período");

    private final String description;

    PeriodType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

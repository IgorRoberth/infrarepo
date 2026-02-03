package com.StoreProject.statisticsrequestdto;

import com.StoreProject.model.MetricType;
import com.StoreProject.model.PeriodType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StatisticsRequestDTO {

    @NotBlank(message = "Nome da métrica é obrigatório")
    private String metricName;

    @NotNull(message = "Tipo da métrica é obrigatório")
    private MetricType metricType;

    private PeriodType periodType;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;

    private BigDecimal numericValue;
    private Long countValue;
    private BigDecimal percentageValue;
    private String textValue;
    private String jsonData;

    private Long entityId;
    private String entityType;

    private Integer expirationHours;
}
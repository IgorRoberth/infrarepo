package com.StoreProject.statisticsrequestdto;

import ch.qos.logback.core.rolling.helper.PeriodicityType;
import com.StoreProject.model.MetricType;
import com.StoreProject.model.PeriodType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StatisticsResponseDTO {

    private Long id;
    private String metricName;
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

    private LocalDateTime calculatedAt;
    private LocalDateTime expiresAt;
    private Boolean isActive;
    private Boolean isExpired;

    public void setPeriodType(PeriodType periodType) {
    }
}
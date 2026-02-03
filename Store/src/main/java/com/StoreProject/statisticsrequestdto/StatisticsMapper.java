package com.StoreProject.statisticsrequestdto;

import com.StoreProject.model.Statistics;
import org.springframework.stereotype.Component;

@Component
public class StatisticsMapper {

    StatisticsResponseDTO dto = new StatisticsResponseDTO();

    public Statistics toEntity(StatisticsRequestDTO dto) {
        Statistics statistics = new Statistics();
        statistics.setMetricName(dto.getMetricName());
        statistics.setMetricType(dto.getMetricType());
        statistics.setPeriodType(dto.getPeriodType());
        statistics.setPeriodStart(dto.getPeriodStart());
        statistics.setPeriodEnd(dto.getPeriodEnd());
        statistics.setNumericValue(dto.getNumericValue());
        statistics.setCountValue(dto.getCountValue());
        statistics.setPercentageValue(dto.getPercentageValue());
        statistics.setTextValue(dto.getTextValue());
        statistics.setJsonData(dto.getJsonData());
        statistics.setEntityId(dto.getEntityId());
        statistics.setEntityType(dto.getEntityType());

        if (dto.getExpirationHours() != null) {
            statistics.setExpirationHours(dto.getExpirationHours());
        }

        return statistics;
    }

    public StatisticsResponseDTO toResponseDTO(Statistics statistics) {
        StatisticsResponseDTO dto = new StatisticsResponseDTO();
        dto.setId(statistics.getId());
        dto.setMetricName(statistics.getMetricName());
        dto.setMetricType(statistics.getMetricType());
        dto.setPeriodType(statistics.getPeriodType());
        dto.setPeriodStart(statistics.getPeriodStart());
        dto.setPeriodEnd(statistics.getPeriodEnd());
        dto.setNumericValue(statistics.getNumericValue());
        dto.setCountValue(statistics.getCountValue());
        dto.setPercentageValue(statistics.getPercentageValue());
        dto.setTextValue(statistics.getTextValue());
        dto.setJsonData(statistics.getJsonData());
        dto.setEntityId(statistics.getEntityId());
        dto.setEntityType(statistics.getEntityType());
        dto.setCalculatedAt(statistics.getCalculatedAt());
        dto.setExpiresAt(statistics.getExpiresAt());
        dto.setIsActive(statistics.getIsActive());
        dto.setIsExpired(statistics.isExpired());

        return dto;
    }
}
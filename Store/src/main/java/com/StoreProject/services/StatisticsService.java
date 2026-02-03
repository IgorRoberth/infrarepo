package com.StoreProject.services;

import com.StoreProject.enums.OrderStatus;
import com.StoreProject.enums.ProductCategory;
import com.StoreProject.model.MetricType;
import com.StoreProject.model.PeriodType;
import com.StoreProject.model.Product;
import com.StoreProject.model.Statistics;
import com.StoreProject.repository.*;
import com.StoreProject.statisticsrequestdto.StatisticsRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class StatisticsService {

    @Autowired
    private StatisticsRepository statisticsRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private SellersRepository sellersRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private TrackingRepository trackingRepository;

    /**
     * Criar ou atualizar estatística
     */
    @Transactional
    public Statistics createOrUpdateStatistic(StatisticsRequestDTO requestDTO) {
        // Verificar se já existe uma estatística similar
        Optional<Statistics> existing = statisticsRepository
            .findByEntityTypeAndEntityIdAndMetricTypeAndIsActiveTrue(
                requestDTO.getEntityType(), 
                requestDTO.getEntityId(), 
                requestDTO.getMetricType()
            );
        
        Statistics statistics;
        if (existing.isPresent()) {
            statistics = existing.get();
            updateStatisticsFromDTO(statistics, requestDTO);
        } else {
            statistics = createNewStatistics(requestDTO);
        }
        
        return statisticsRepository.save(statistics);
    }
    
    private Statistics createNewStatistics(StatisticsRequestDTO dto) {
        Statistics statistics = new Statistics();
        updateStatisticsFromDTO(statistics, dto);
        return statistics;
    }
    
    private void updateStatisticsFromDTO(Statistics statistics, StatisticsRequestDTO dto) {
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
        statistics.setCalculatedAt(LocalDateTime.now());
        
        if (dto.getExpirationHours() != null) {
            statistics.setExpirationHours(dto.getExpirationHours());
        }
    }

    /**
     * Calcular e salvar estatísticas do dashboard
     */
    @Transactional
    public void calculateAndSaveDashboardStatistics() {
        LocalDateTime now = LocalDateTime.now();
        
        // Total de clientes
        saveStatistic("Total de Clientes", MetricType.TOTAL_CUSTOMERS, 
                     null, customerRepository.count(), null, null, null, 24);
        
        // Total de vendedores
        saveStatistic("Total de Vendedores", MetricType.TOTAL_SELLERS, 
                     null, sellersRepository.count(), null, null, null, 24);
        
        // Total de produtos
        saveStatistic("Total de Produtos", MetricType.TOTAL_PRODUCTS, 
                     null, productRepository.count(), null, null, null, 24);
        
        // Total de pedidos
        saveStatistic("Total de Pedidos", MetricType.TOTAL_ORDERS, 
                     null, orderRepository.count(), null, null, null, 24);
        
        // Receita total
        BigDecimal receitaTotal = orderRepository.sumTotalAmountByStatus(
            OrderStatus.ENTREGUE
        );
        saveStatistic("Receita Total", MetricType.TOTAL_REVENUE, 
                     receitaTotal != null ? receitaTotal : BigDecimal.ZERO, 
                     null, null, null, null, 24);
    }
    
    private void saveStatistic(String name, MetricType type, BigDecimal numericValue, 
                              Long countValue, BigDecimal percentageValue, 
                              String textValue, String jsonData, int expirationHours) {
        Statistics stat = new Statistics();
        stat.setMetricName(name);
        stat.setMetricType(type);
        stat.setPeriodType(PeriodType.REAL_TIME);
        stat.setNumericValue(numericValue);
        stat.setCountValue(countValue);
        stat.setPercentageValue(percentageValue);
        stat.setTextValue(textValue);
        stat.setJsonData(jsonData);
        stat.setExpirationHours(expirationHours);
        
        statisticsRepository.save(stat);
    }

    /**
     * Buscar estatísticas por tipo
     */
    public List<Statistics> getStatisticsByType(MetricType metricType) {
        return statisticsRepository.findByMetricTypeAndIsActiveTrue(metricType);
    }

    /**
     * Buscar estatísticas válidas
     */
    public List<Statistics> getValidStatistics() {
        return statisticsRepository.findValidStatistics(LocalDateTime.now());
    }

    /**
     * Limpar estatísticas expiradas
     */
    @Transactional
    public int cleanExpiredStatistics() {
        return statisticsRepository.deactivateExpiredStatistics(LocalDateTime.now());
    }

    /**
     * Dashboard com cache
     */
    public Map<String, Object> getDashboardData() {
        Map<String, Object> dashboard = new HashMap<>();
        
        // Tentar buscar do cache primeiro
        List<Statistics> cachedStats = statisticsRepository.findValidStatistics(LocalDateTime.now());
        
        if (cachedStats.isEmpty()) {
            // Se não há cache, calcular e salvar
            calculateAndSaveDashboardStatistics();
            cachedStats = statisticsRepository.findValidStatistics(LocalDateTime.now());
        }
        
        // Converter para mapa
        for (Statistics stat : cachedStats) {
            String key = stat.getMetricType().name().toLowerCase();
            
            if (stat.getNumericValue() != null) {
                dashboard.put(key, stat.getNumericValue());
            } else if (stat.getCountValue() != null) {
                dashboard.put(key, stat.getCountValue());
            } else if (stat.getPercentageValue() != null) {
                dashboard.put(key, stat.getPercentageValue());
            } else if (stat.getTextValue() != null) {
                dashboard.put(key, stat.getTextValue());
            }
        }
        
        return dashboard;
    }

    /**
     * Estatísticas de vendas por período
     */
    public Map<String, Object> getSalesStatistics(LocalDateTime inicio, LocalDateTime fim) {
        List<Object[]> results = statisticsRepository.getSalesStatistics(inicio, fim);

        Map<String, Object> stats = new HashMap<>();

        if (!results.isEmpty()) {
            Object[] result = results.get(0);
            stats.put("totalPedidos", result[0]);
            stats.put("receitaTotal", result[1]);
            stats.put("ticketMedio", result[2]);
            stats.put("clientesUnicos", result[3]);
        } else {
            stats.put("totalPedidos", 0L);
            stats.put("receitaTotal", BigDecimal.ZERO);
            stats.put("ticketMedio", BigDecimal.ZERO);
            stats.put("clientesUnicos", 0L);
        }

        return stats;
    }

    /**
     * Top produtos mais vendidos
     */
    public Map<String, Object> getTopProducts(int limit, LocalDateTime inicio, LocalDateTime fim) {
        List<Object[]> results = statisticsRepository.getTopSellingProducts(inicio, fim, limit);

        Map<String, Object> topProducts = new HashMap<>();
        topProducts.put("produtos", results);
        topProducts.put("periodo", Map.of("inicio", inicio, "fim", fim));

        return topProducts;
    }

    /**
     * Estatísticas por vendedor
     */
    public Map<String, Object> getSellerStatistics(LocalDateTime inicio, LocalDateTime fim) {
        List<Object[]> results = statisticsRepository.getSellerStatistics(inicio, fim);

        Map<String, Object> stats = new HashMap<>();
        stats.put("vendedores", results);
        stats.put("periodo", Map.of("inicio", inicio, "fim", fim));

        return stats;
    }

    /**
     * Estatísticas de entrega
     */
    public Map<String, Object> getDeliveryStatistics() {
        Map<String, Object> stats = new HashMap<>();

        Long delivered = trackingRepository.countDelayedDeliveries();
        Long pending = trackingRepository.countPendingDeliveries();
        Double avgDeliveryTime = trackingRepository.getAverageDeliveryTimeInDays();

        stats.put("entregasRealizadas", delivered != null ? delivered : 0L);
        stats.put("entregasPendentes", pending != null ? pending : 0L);
        stats.put("tempoMedioEntrega", avgDeliveryTime != null ? avgDeliveryTime : 0.0);

        // Calcular taxa de entrega
        Long total = (delivered != null ? delivered : 0L) + (pending != null ? pending : 0L);
        double taxaEntrega = total > 0 ? (delivered != null ? delivered : 0L) * 100.0 / total : 0.0;
        stats.put("taxaEntrega", taxaEntrega);

        return stats;
    }

    /**
     * Receita por período
     */
    public Map<String, Object> getRevenueData(LocalDateTime inicio, LocalDateTime fim) {
        BigDecimal revenue = orderRepository.calculateRevenueBetween(inicio, fim);

        Map<String, Object> revenueData = new HashMap<>();
        revenueData.put("receita", revenue != null ? revenue : BigDecimal.ZERO);
        revenueData.put("periodo", Map.of("inicio", inicio, "fim", fim));

        return revenueData;
    }

    /**
     * Estatísticas por categoria de produto
     */
    public Map<String, Object> getCategoryStatistics() {
    List<ProductCategory> categorias = productRepository.findDistinctCategorias();

    Map<String, Object> stats = new HashMap<>();

    for (ProductCategory categoria : categorias) {
        List<Product> count = productRepository.findByCategoriaAndAtivoTrue(categoria);
        stats.put(categoria.name(), count); // .name() para usar como String no map
    }

    return stats;
}

    /**
     * Relatório de produtos com estoque baixo
     */
    public Map<String, Object> getLowStockReport(Integer threshold) {
        List<com.StoreProject.model.Product> lowStockProducts = productRepository.findByEstoqueLessThanEqualAndAtivoTrue(threshold);

        Map<String, Object> report = new HashMap<>();
        report.put("produtos", lowStockProducts);
        report.put("limite", threshold);
        report.put("total", lowStockProducts.size());

        return report;
    }
}

package com.StoreProject.repository;

import com.StoreProject.model.MetricType;
import com.StoreProject.model.PeriodType;
import com.StoreProject.model.Statistics;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StatisticsRepository extends JpaRepository<Statistics, Long> {
    
    // Buscar por tipo de métrica
    List<Statistics> findByMetricTypeAndIsActiveTrue(MetricType metricType);
    
    // Buscar por período
    List<Statistics> findByPeriodTypeAndIsActiveTrue(PeriodType periodType);
    
    // Buscar por entidade
    List<Statistics> findByEntityTypeAndEntityIdAndIsActiveTrue(String entityType, Long entityId);
    
    // Buscar métricas válidas (não expiradas)
    @Query("SELECT s FROM Statistics s WHERE s.isActive = true AND " +
           "(s.expiresAt IS NULL OR s.expiresAt > :now)")
    List<Statistics> findValidStatistics(@Param("now") LocalDateTime now);
    
    // Buscar por nome da métrica
    List<Statistics> findByMetricNameAndIsActiveTrue(String metricName);
    
    // Buscar última estatística por tipo
    @Query("SELECT s FROM Statistics s WHERE s.metricType = :metricType " +
           "AND s.isActive = true ORDER BY s.calculatedAt DESC")
    List<Statistics> findLatestByMetricType(@Param("metricType") MetricType metricType);
    
    // Buscar por período específico
    List<Statistics> findByPeriodStartBetweenAndIsActiveTrue(
        LocalDateTime start, LocalDateTime end
    );
    
    // Buscar estatísticas expiradas
    @Query("SELECT s FROM Statistics s WHERE s.expiresAt IS NOT NULL " +
           "AND s.expiresAt <= :now AND s.isActive = true")
    List<Statistics> findExpiredStatistics(@Param("now") LocalDateTime now);
    
    // Limpar estatísticas expiradas
    @Modifying
    @Transactional
    @Query("""
    UPDATE Statistics s
    SET s.isActive = false
    WHERE s.expiresAt IS NOT NULL
    AND s.expiresAt <= :now
    """)
    int deactivateExpiredStatistics(@Param("now") LocalDateTime now);

    
    // Buscar por múltiplos tipos
    List<Statistics> findByMetricTypeInAndIsActiveTrue(List<MetricType> metricTypes);
    
    // Buscar com paginação
    Page<Statistics> findByIsActiveTrueOrderByCalculatedAtDesc(Pageable pageable);
    
    // Contar por tipo
    Long countByMetricTypeAndIsActiveTrue(MetricType metricType);
    
    // Buscar estatísticas recentes
    @Query("SELECT s FROM Statistics s WHERE s.isActive = true " +
           "ORDER BY s.calculatedAt DESC")
    List<Statistics> findRecentStatistics(Pageable pageable);
    
    // Buscar por entidade e tipo
    Optional<Statistics> findByEntityTypeAndEntityIdAndMetricTypeAndIsActiveTrue(
        String entityType, Long entityId, MetricType metricType
    );
    
    // Deletar estatísticas antigas
    @Modifying
    @Transactional
    @Query("DELETE FROM Statistics s WHERE s.calculatedAt < :cutoffDate")
    int deleteOldStatistics(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // RELATÓRIOS COM QUERIES NATIVAS
    
    // Estatísticas de vendas por período
    @Query(value = "SELECT " +
           "COUNT(DISTINCT o.id) as totalPedidos, " +
           "COALESCE(SUM(o.total_amount), 0) as receitaTotal, " +
           "COALESCE(AVG(o.total_amount), 0) as ticketMedio, " +
           "COUNT(DISTINCT o.customer_id) as clientesUnicos " +
           "FROM orders o " +
           "WHERE o.criado_em BETWEEN :inicio AND :fim " +
           "AND o.status = 'ENTREGUE'", nativeQuery = true)
    List<Object[]> getSalesStatistics(
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );
    
    // Top produtos por vendas
    @Query(value = """
    SELECT p.nome,
           COALESCE(SUM(oi.quantidade), 0) as quantidadeVendida,
           COALESCE(SUM(oi.subtotal), 0) as receitaProduto
    FROM products p
    LEFT JOIN order_items oi ON p.id = oi.product_id
    LEFT JOIN orders o ON oi.order_id = o.id
    WHERE (o.criado_em IS NULL OR o.criado_em BETWEEN :inicio AND :fim)
    GROUP BY p.id, p.nome
    ORDER BY quantidadeVendida DESC
    LIMIT 10
    """, nativeQuery = true)
    List<Object[]> getTopSellingProducts(
       @Param("inicio") LocalDateTime inicio,
       @Param("fim") LocalDateTime fim,
       @Param("limit") int limit
     );
    
    // Estatísticas por vendedor
    @Query(value = "SELECT " +
           "s.nome as vendedor, " +
           "COUNT(DISTINCT o.id) as pedidos, " +
           "COALESCE(SUM(oi.subtotal), 0) as receita, " +
           "COALESCE(SUM(oi.quantidade), 0) as itensVendidos " +
           "FROM sellers s " +
           "LEFT JOIN products p ON s.id = p.seller_id " +
           "LEFT JOIN order_items oi ON p.id = oi.product_id " +
           "LEFT JOIN orders o ON oi.order_id = o.id " +
           "WHERE (o.criado_em IS NULL OR o.criado_em BETWEEN :inicio AND :fim) " +
           "GROUP BY s.id, s.nome " +
           "ORDER BY receita DESC", nativeQuery = true)
    List<Object[]> getSellerStatistics(
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );
}
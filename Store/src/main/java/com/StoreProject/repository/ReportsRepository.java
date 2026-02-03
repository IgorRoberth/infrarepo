package com.StoreProject.repository;

import com.StoreProject.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReportsRepository extends JpaRepository<Order, Long> {
    
    @Query(value = "SELECT " +
           "COUNT(DISTINCT o.id) as totalPedidos, " +
           "COALESCE(SUM(o.total_amount), 0) as receitaTotal " +
           "FROM orders o " +
           "WHERE o.criado_em BETWEEN :inicio AND :fim", nativeQuery = true)
    List<Object[]> getOrderStatistics(
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );
}
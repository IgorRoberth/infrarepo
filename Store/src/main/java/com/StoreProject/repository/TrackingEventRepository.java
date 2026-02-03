package com.StoreProject.repository;

import com.StoreProject.enums.OrderStatus;
import com.StoreProject.model.TrackingEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrackingEventRepository extends JpaRepository<TrackingEvent, Long> {

    // Buscar por ID do tracking
    List<TrackingEvent> findByTrackingId(Long trackingId);
    List<TrackingEvent> findByTrackingIdOrderByDataEventoDesc(Long trackingId);
    
    // Buscar por status
    Page<TrackingEvent> findByStatus(OrderStatus status, Pageable pageable);
    List<TrackingEvent> findByStatus(OrderStatus status);
    
    // Buscar por período
    List<TrackingEvent> findByDataEventoBetween(LocalDateTime inicio, LocalDateTime fim);
    
    // Buscar por localização (campo string)
    List<TrackingEvent> findByLocalizacaoContainingIgnoreCase(String localizacao);
    
    // Buscar por descrição (campo string)
    List<TrackingEvent> findByDescricaoContainingIgnoreCase(String descricao);
    
    // Buscar por código de rastreamento usando query customizada
    @Query("SELECT te FROM TrackingEvent te " +
           "WHERE te.tracking.codigoRastreamento LIKE %:codigo% " +
           "ORDER BY te.dataEvento DESC")
    List<TrackingEvent> findByCodigoRastreamentoContaining(@Param("codigo") String codigo);
    
    // Buscar por código exato
    @Query("SELECT te FROM TrackingEvent te " +
           "WHERE te.tracking.codigoRastreamento = :codigo " +
           "ORDER BY te.dataEvento DESC")
    List<TrackingEvent> findByCodigoRastreamento(@Param("codigo") String codigo);
    
    // Eventos recentes
    List<TrackingEvent> findTop10ByOrderByDataEventoDesc();
    
    // Buscar por vendedor usando query customizada
    @Query("SELECT DISTINCT te FROM TrackingEvent te " +
           "JOIN te.tracking t " +
           "JOIN t.order o " +
           "JOIN o.orderItems oi " +
           "JOIN oi.product p " +
           "WHERE p.seller.id = :sellerId " +
           "ORDER BY te.dataEvento DESC")
    List<TrackingEvent> findByTrackingOrderProductSellerId(@Param("sellerId") Long sellerId);
    
    // Buscar eventos por cliente
    @Query("SELECT te FROM TrackingEvent te " +
           "JOIN te.tracking t " +
           "JOIN t.order o " +
           "WHERE o.customer.id = :customerId " +
           "ORDER BY te.dataEvento DESC")
    List<TrackingEvent> findByCustomerId(@Param("customerId") Long customerId);
    
    // Buscar por transportadora (campo string)
    @Query("SELECT te FROM TrackingEvent te " +
           "JOIN te.tracking t " +
           "WHERE t.transportadora LIKE %:transportadora% " +
           "ORDER BY te.dataEvento DESC")
    List<TrackingEvent> findByTransportadoraContaining(@Param("transportadora") String transportadora);
    
    // Contar eventos por status
    Long countByStatus(OrderStatus status);
    
    // Buscar eventos por período e status
    @Query("SELECT te FROM TrackingEvent te " +
           "WHERE te.dataEvento BETWEEN :inicio AND :fim " +
           "AND te.status = :status " +
           "ORDER BY te.dataEvento DESC")
    List<TrackingEvent> findByDataEventoBetweenAndStatus(
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim,
        @Param("status") OrderStatus status);
}
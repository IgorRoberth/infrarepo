package com.StoreProject.repository;

import com.StoreProject.enums.OrderStatus;
import com.StoreProject.model.Tracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrackingRepository extends JpaRepository<Tracking, Long> {

    Optional<Tracking> findByOrderId(Long orderId);
    Optional<Tracking> findByCodigoRastreamento(String codigoRastreamento);

    List<Tracking> findByStatus(OrderStatus status);
    List<Tracking> findByTransportadoraContainingIgnoreCase(String transportadora);

    @Query("SELECT t FROM Tracking t WHERE t.status <> :entregue AND t.status <> :cancelado")
    List<Tracking> findActiveTrackings(
        @Param("entregue") OrderStatus entregue,
        @Param("cancelado") OrderStatus cancelado
    );

    @Query("""
        SELECT t FROM Tracking t
        JOIN t.order o
        JOIN o.orderItems oi
        JOIN oi.product p
        WHERE p.seller.id = :sellerId
    """)
    List<Tracking> findByOrderItemsProductSellerId(@Param("sellerId") Long sellerId);

    // ---------- CONTADORES ----------

    Long countByStatus(OrderStatus status);

    @Query(value = """
        SELECT COUNT(*) 
        FROM tracking
        WHERE status IN ('PROCESSANDO', 'ENVIADO', 'EM_TRANSITO')
    """, nativeQuery = true)
    Long countPendingDeliveries();

    @Query(value = """
        SELECT COUNT(*) 
        FROM tracking
        WHERE status IN ('ENVIADO', 'EM_TRANSITO')
          AND data_entrega_prevista < CURDATE()
    """, nativeQuery = true)
    Long countDelayedDeliveries();

    // ---------- MÉTRICAS ----------

    @Query(value = """
        SELECT AVG(DATEDIFF(data_entrega_real, data_envio))
        FROM tracking
        WHERE status = 'ENTREGUE'
          AND data_envio IS NOT NULL
          AND data_entrega_real IS NOT NULL
    """, nativeQuery = true)
    Double getAverageDeliveryTimeInDays();

    @Query(value = """
        SELECT transportadora,
               AVG(DATEDIFF(data_entrega_real, data_envio))
        FROM tracking
        WHERE status = 'ENTREGUE'
          AND transportadora IS NOT NULL
          AND data_envio IS NOT NULL
          AND data_entrega_real IS NOT NULL
        GROUP BY transportadora
    """, nativeQuery = true)
    List<Object[]> getAverageDeliveryTimeByTransportadora();

    // ---------- PERÍODO ----------

    @Query("SELECT t FROM Tracking t WHERE t.dataEnvio BETWEEN :inicio AND :fim")
    List<Tracking> findByDataEnvioBetween(
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );

    @Query("SELECT t FROM Tracking t WHERE t.status = :status AND t.dataEntregaReal BETWEEN :inicio AND :fim")
    List<Tracking> findDeliveredBetween(
        @Param("status") OrderStatus status,
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );
}
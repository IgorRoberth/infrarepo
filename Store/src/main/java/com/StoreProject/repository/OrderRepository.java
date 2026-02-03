package com.StoreProject.repository;

import com.StoreProject.model.Order;
import com.StoreProject.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Buscar pedido por número
     */
    Optional<Order> findByOrderNumber(String orderNumber);
    
    /**
     * Buscar pedido por ID com tracking e customer carregados
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.tracking LEFT JOIN FETCH o.customer WHERE o.id = :id")
    Optional<Order> findByIdWithTracking(@Param("id") Long id);
    
    // ===== MÉTODOS PARA CUSTOMER =====
    
    /**
     * Buscar pedidos por customer
     */
    @EntityGraph(attributePaths = {"tracking", "customer"})
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);
    
    /**
     * Buscar pedidos por customer e status
     */
    @EntityGraph(attributePaths = {"tracking", "customer"})
    Page<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status, Pageable pageable);
    
    /**
     * Buscar pedidos por customer com tracking
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.tracking LEFT JOIN FETCH o.customer " +
           "WHERE o.customer.id = :customerId")
    Page<Order> findByCustomerIdWithTracking(@Param("customerId") Long customerId, Pageable pageable);
    
    /**
     * Buscar pedidos por customer e status com tracking
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.tracking LEFT JOIN FETCH o.customer " +
           "WHERE o.customer.id = :customerId AND o.status = :status")
    Page<Order> findByCustomerIdAndStatusWithTracking(
        @Param("customerId") Long customerId, 
        @Param("status") OrderStatus status, 
        Pageable pageable);
    
    // ===== MÉTODOS PARA SELLER =====
    
    /**
     * Buscar pedidos por seller (através dos produtos)
     */
    @Query("SELECT DISTINCT o FROM Order o " +
           "JOIN o.orderItems oi " +
           "JOIN oi.product p " +
           "WHERE p.seller.id = :sellerId")
    Page<Order> findByOrderItemsProductSellerId(@Param("sellerId") Long sellerId, Pageable pageable);
    
    /**
     * Buscar pedidos por seller e status
     */
    @Query("SELECT DISTINCT o FROM Order o " +
           "JOIN o.orderItems oi " +
           "JOIN oi.product p " +
           "WHERE p.seller.id = :sellerId AND o.status = :status")
    Page<Order> findByOrderItemsProductSellerIdAndStatus(
        @Param("sellerId") Long sellerId, 
        @Param("status") OrderStatus status, 
        Pageable pageable);
    
    /**
     * Buscar pedidos por seller com tracking
     */
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.tracking " +
           "LEFT JOIN FETCH o.customer " +
           "JOIN o.orderItems oi " +
           "JOIN oi.product p " +
           "WHERE p.seller.id = :sellerId")
    Page<Order> findBySellerIdWithTracking(@Param("sellerId") Long sellerId, Pageable pageable);
    
    /**
     * Buscar pedidos por seller e status com tracking
     */
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.tracking " +
           "LEFT JOIN FETCH o.customer " +
           "JOIN o.orderItems oi " +
           "JOIN oi.product p " +
           "WHERE p.seller.id = :sellerId AND o.status = :status")
    Page<Order> findBySellerIdAndStatusWithTracking(
        @Param("sellerId") Long sellerId, 
        @Param("status") OrderStatus status, 
        Pageable pageable);
    
    // ===== MÉTODOS POR STATUS =====
    
    /**
     * Buscar pedidos por status
     */
    @EntityGraph(attributePaths = {"tracking", "customer"})
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"tracking", "customer"})
    Page<Order> findAll(Pageable pageable);
    
    /**
     * Buscar pedidos por status com tracking
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.tracking LEFT JOIN FETCH o.customer " +
           "WHERE o.status = :status")
    Page<Order> findByStatusWithTracking(@Param("status") OrderStatus status, Pageable pageable);
    
    /**
     * Buscar todos os pedidos com tracking
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.tracking LEFT JOIN FETCH o.customer")
    Page<Order> findAllWithTracking(Pageable pageable);
    
    // ===== MÉTODOS PARA RELATÓRIOS E ESTATÍSTICAS =====
    
    /**
     * Buscar pedidos recentes (últimos 10)
     */
    List<Order> findTop10ByOrderByCriadoEmDesc();
    
    /**
     * Buscar pedidos por período
     */
    @Query("SELECT o FROM Order o WHERE o.criadoEm BETWEEN :startDate AND :endDate")
    List<Order> findByDateRange(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate);
    
    /**
     * Contar pedidos por status
     */
    long countByStatus(OrderStatus status);
    
    /**
     * Contar pedidos por customer
     */
    long countByCustomerId(Long customerId);
    
    /**
     * Contar pedidos por seller
     */
    @Query("SELECT COUNT(DISTINCT o) FROM Order o " +
           "JOIN o.orderItems oi " +
           "JOIN oi.product p " +
           "WHERE p.seller.id = :sellerId")
    long countByOrderItemsProductSellerId(@Param("sellerId") Long sellerId);

    
    /**
     * Buscar pedidos enviados para um customer (para confirmação de entrega)
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.tracking " +
           "WHERE o.customer.id = :customerId AND o.status = :status")
    List<Order> findByCustomerIdAndStatusForDelivery(
        @Param("customerId") Long customerId, 
        @Param("status") OrderStatus status);
    
    /**
     * Buscar pedidos que podem ser confirmados como entregues
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.tracking " +
           "WHERE o.status IN ('ENVIADO', 'EM_TRANSITO') " +
           "AND o.customer.id = :customerId")
    List<Order> findDeliverableOrdersByCustomer(@Param("customerId") Long customerId);
    
    /**
     * Buscar pedidos entregues por período (para relatórios)
     */
    @Query("SELECT o FROM Order o WHERE o.status = 'ENTREGUE' " +
           "AND o.atualizadoEm BETWEEN :startDate AND :endDate")
    List<Order> findDeliveredOrdersByDateRange(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate);
    
    // ===== MÉTODOS PARA VALIDAÇÃO DE PERMISSÕES =====
    
    /**
     * Verificar se um pedido pertence a um customer
     */
    @Query("SELECT COUNT(o) > 0 FROM Order o WHERE o.id = :orderId AND o.customer.id = :customerId")
    boolean existsByIdAndCustomerId(@Param("orderId") Long orderId, @Param("customerId") Long customerId);
    
    /**
     * Verificar se um pedido tem produtos de um seller
     */
    @Query("SELECT COUNT(o) > 0 FROM Order o " +
           "JOIN o.orderItems oi " +
           "JOIN oi.product p " +
           "WHERE o.id = :orderId AND p.seller.id = :sellerId")
    boolean existsByIdAndSellerId(@Param("orderId") Long orderId, @Param("sellerId") Long sellerId);
    
    // ===== MÉTODOS PARA AUDITORIA =====
    
    /**
     * Buscar pedidos modificados recentemente
     */
    @Query("SELECT o FROM Order o WHERE o.atualizadoEm >= :since ORDER BY o.atualizadoEm DESC")
    List<Order> findRecentlyModified(@Param("since") LocalDateTime since);
    
    /**
     * Buscar pedidos por status que foram atualizados em um período
     */
    @Query("SELECT o FROM Order o WHERE o.status = :status " +
           "AND o.atualizadoEm BETWEEN :startDate AND :endDate")
    List<Order> findByStatusAndUpdateDateRange(
        @Param("status") OrderStatus status,
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate);

    // ===== MÉTODOS PARA RELATÓRIOS E ESTATÍSTICAS =====
    
    /**
     * Calcular receita total entre datas
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
           "WHERE o.criadoEm BETWEEN :inicio AND :fim " +
           "AND o.status = 'ENTREGUE'")
    BigDecimal calculateRevenueBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    /**
     * Somar valor total por status
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = :status")
    BigDecimal sumTotalAmountByStatus(@Param("status") OrderStatus status);
    
    /**
     * Buscar pedido por ID com orderItems carregados
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.product LEFT JOIN FETCH o.customer LEFT JOIN FETCH o.tracking WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);
    
    /**
     * Buscar pedidos por customer com orderItems
     */
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.product WHERE o.customer.id = :customerId")
    List<Order> findByCustomerIdWithItems(@Param("customerId") Long customerId);
    
    /**
     * Buscar pedidos por seller com orderItems
     */
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.product p WHERE p.seller.id = :sellerId")
    List<Order> findBySellerIdWithItems(@Param("sellerId") Long sellerId);
}
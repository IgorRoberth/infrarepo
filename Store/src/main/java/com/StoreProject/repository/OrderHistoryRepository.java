package com.StoreProject.repository;

import com.StoreProject.enums.OrderStatus;
import com.StoreProject.model.OrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {

    // Buscar histórico por pedido ordenado por createdAt
    List<OrderHistory> findByOrder_IdOrderByCreatedAtDesc(Long orderId);

    Optional<OrderHistory> findFirstByOrder_IdOrderByCreatedAtDesc(Long orderId);

    // Buscar por status
    List<OrderHistory> findByNewStatus(OrderStatus status);
    List<OrderHistory> findByOldStatus(OrderStatus status);

    // Buscar por quem fez a mudança
    List<OrderHistory> findByChangedBy(String changedBy);
    List<OrderHistory> findByChangedById(Long changedById);

    // Buscar por período (createdAt entre duas datas)
    List<OrderHistory> findByCreatedAtBetween(LocalDateTime inicio, LocalDateTime fim);

    // Buscar mudanças específicas
    @Query("""
           SELECT oh
           FROM OrderHistory oh
           WHERE oh.order.id = :orderId
             AND oh.oldStatus = :oldStatus
             AND oh.newStatus = :newStatus
           """)
    List<OrderHistory> findStatusTransition(@Param("orderId") Long orderId,
                                            @Param("oldStatus") OrderStatus oldStatus,
                                            @Param("newStatus") OrderStatus newStatus);

    // Contar mudanças por pedido
    Long countByOrder_Id(Long orderId);

    // Buscar últimas mudanças
    @Query("SELECT oh FROM OrderHistory oh ORDER BY oh.createdAt DESC")
    List<OrderHistory> findRecentChanges();
}

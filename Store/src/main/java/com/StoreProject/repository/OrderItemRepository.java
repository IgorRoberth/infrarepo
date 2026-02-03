package com.StoreProject.repository;

import com.StoreProject.model.Order;
import com.StoreProject.model.OrderItem;
import com.StoreProject.model.Product;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrder(Order order);
    List<OrderItem> findByOrderId(Long orderId);

    List<OrderItem> findByProduct(Product product);
    List<OrderItem> findByProductId(Long productId);

    List<OrderItem> findByOrderAndProduct(Order order, Product product);

    @Query("SELECT SUM(oi.quantidade) FROM OrderItem oi WHERE oi.product.id = :productId")
    Long getTotalQuantitySoldByProduct(@Param("productId") Long productId);

    @Query("SELECT SUM(oi.subtotal) FROM OrderItem oi WHERE oi.product.id = :productId")
    BigDecimal getTotalRevenueByProduct(@Param("productId") Long productId);

    @Query("""
        SELECT oi.product, SUM(oi.quantidade)
        FROM OrderItem oi
        GROUP BY oi.product
        ORDER BY SUM(oi.quantidade) DESC
    """)
    List<Object[]> findMostSoldProducts();

    @Query("SELECT oi FROM OrderItem oi WHERE oi.product.seller.id = :sellerId")
    List<OrderItem> findByProductSellerId(@Param("sellerId") Long sellerId);

    @Query("SELECT SUM(oi.subtotal) FROM OrderItem oi WHERE oi.product.seller.id = :sellerId")
    BigDecimal getTotalRevenueBySeller(@Param("sellerId") Long sellerId);

    @Query("SELECT SUM(oi.quantidade) FROM OrderItem oi WHERE oi.product.seller.id = :sellerId")
    Long getTotalQuantitySoldBySeller(@Param("sellerId") Long sellerId);

    @Modifying
    @Transactional
    void deleteByOrderId(Long orderId);
}
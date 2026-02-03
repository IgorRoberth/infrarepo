package com.StoreProject.services;

import com.StoreProject.enums.OrderStatus;

import com.StoreProject.model.OrderItem;
import com.StoreProject.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderItemService {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    public OrderItem findById(Long id) {
        return orderItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item do pedido não encontrado: " + id));
    }

    public List<OrderItem> findByOrder(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    public List<OrderItem> findByProduct(Long productId) {
        return orderItemRepository.findByProductId(productId);
    }

    @Transactional
    public OrderItem updateQuantidade(Long id, Integer novaQuantidade) {
        OrderItem item = findById(id);
        
        // ✅ AGORA FUNCIONA - Import correto
        if (item.getOrder().getStatus() != OrderStatus.PENDENTE) {
            throw new RuntimeException("Só é possível alterar itens de pedidos pendentes");
        }
        
        if (novaQuantidade <= 0) {
            throw new RuntimeException("Quantidade deve ser maior que zero");
        }
        
        // Verificar estoque disponível
        if (item.getProduct().getEstoque() < novaQuantidade) {
            throw new RuntimeException("Estoque insuficiente. Disponível: " + item.getProduct().getEstoque());
        }
        
        // Atualizar quantidade e recalcular subtotal
        item.setQuantidade(novaQuantidade);
        item.setSubtotal(item.getPrecoUnitario().multiply(BigDecimal.valueOf(novaQuantidade)));
        
        OrderItem updatedItem = orderItemRepository.save(item);
        
        // Recalcular total do pedido
        recalculateOrderTotal(item.getOrder().getId());
        
        return updatedItem;
    }

    private void recalculateOrderTotal(Long orderId) {
        List<OrderItem> items = findByOrder(orderId);
        BigDecimal total = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Atualizar total do pedido
        orderService.updateTotal(orderId, total);
    }

    @Transactional
    public void delete(Long id) {
        OrderItem item = findById(id);
        
        if (item.getOrder().getStatus() != OrderStatus.PENDENTE) {
            throw new RuntimeException("Só é possível remover itens de pedidos pendentes");
        }
        
        orderItemRepository.delete(item);
        recalculateOrderTotal(item.getOrder().getId());
    }

    public Map<String, Object> getProductStatistics(Long productId) {
        Map<String, Object> stats = new HashMap<>();

        Long totalQuantity = orderItemRepository.getTotalQuantitySoldByProduct(productId);
        BigDecimal totalRevenue = orderItemRepository.getTotalRevenueByProduct(productId);

        stats.put("totalQuantidadeVendida", totalQuantity != null ? totalQuantity : 0L);
        stats.put("receitaTotal", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

        return stats;
    }

    public List<OrderItem> findBySeller(Long sellerId) {
        return orderItemRepository.findByProductSellerId(sellerId);
    }

    public Map<String, Object> getSellerStatistics(Long sellerId) {
        Map<String, Object> stats = new HashMap<>();

        BigDecimal totalRevenue = orderItemRepository.getTotalRevenueBySeller(sellerId);
        Long totalQuantity = orderItemRepository.getTotalQuantitySoldBySeller(sellerId);

        stats.put("receitaTotal", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        stats.put("totalQuantidadeVendida", totalQuantity != null ? totalQuantity : 0L);

        return stats;
    }

    public List<Object[]> getMostSoldProducts() {
        return orderItemRepository.findMostSoldProducts();
    }
}
package com.StoreProject.services;

import com.StoreProject.enums.OrderStatus;
import com.StoreProject.model.*;
import com.StoreProject.orderdto.*;
import com.StoreProject.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private TrackingRepository trackingRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Criar novo pedido - CORRIGIDO
     */
    @Transactional
    public Order create(OrdersRequestDTO orderDTO) {

        // Buscar cliente
        Customer customer = customerRepository.findById(orderDTO.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado: " + orderDTO.getCustomerId()));

        // Criar pedido
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setCustomer(customer);
        order.setEnderecoEntrega(orderDTO.getEnderecoEntrega());
        order.setStatus(OrderStatus.PENDENTE);
        order.setCriadoEm(LocalDateTime.now());
        order.setAtualizadoEm(LocalDateTime.now());
        
        // INICIALIZAR TOTAL AMOUNT
        order.setTotalAmount(BigDecimal.ZERO);
        
        // Salvar pedido primeiro para obter ID
        order = orderRepository.save(order);
        System.out.println("Pedido criado com ID: " + order.getId());
        
        

        // CALCULAR TOTAL DOS ITENS
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        // Criar itens do pedido
        for (OrderItemRequestDTO itemDTO : orderDTO.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + itemDTO.getProductId()));

            // Verificar disponibilidade
            if (!product.isDisponivelParaVenda()) {
                throw new RuntimeException("Produto não disponível: " + product.getNome());
            }

            // Verificar estoque
            if (product.getEstoque() < itemDTO.getQuantidade()) {
                throw new RuntimeException("Estoque insuficiente para: " + product.getNome() + 
                    ". Disponível: " + product.getEstoque() + ", Solicitado: " + itemDTO.getQuantidade());
            }

            // Criar item do pedido
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantidade(itemDTO.getQuantidade());
            orderItem.setPrecoUnitario(product.getPrecoAtual()); 
            
            BigDecimal subtotal = orderItem.getPrecoUnitario().multiply(BigDecimal.valueOf(itemDTO.getQuantidade()));
            orderItem.setSubtotal(subtotal);
            
            // Salvar item
            orderItemRepository.save(orderItem);
            totalAmount = totalAmount.add(subtotal);
            
            // Reduzir estoque
            product.reduzirEstoque(itemDTO.getQuantidade());
            productRepository.save(product);
            
            System.out.println("Item adicionado: " + product.getNome() + " - Qtd: " + itemDTO.getQuantidade() + " - Subtotal: " + subtotal);
        }
        
        // DEFINIR TOTAL AMOUNT NO PEDIDO
        order.setTotalAmount(totalAmount);
        order = orderRepository.save(order);
        
        System.out.println("Total do pedido: " + totalAmount);

        // Criar tracking
        Tracking tracking = new Tracking();
        tracking.setOrder(order);
        tracking.setStatus(OrderStatus.PENDENTE);
        tracking.setObservacoes(orderDTO.getObservacoes());
        tracking.setAtualizadoEm(LocalDateTime.now());
        trackingRepository.save(tracking);

        return order;
    }

    public Page<Order> findAll(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        // Carregar orderItems para cada pedido
        orders.getContent().forEach(order -> {
            if (order.getOrderItems() != null) {
                // Forçar carregamento dos itens
                order.getOrderItems().size();
                order.getOrderItems().forEach(item -> {
                    if (item.getProduct() != null) {
                        item.getProduct().getNome(); // Forçar carregamento do produto
                    }
                });
            }
        });
        return orders;
    }

    public Page<Order> findByStatusPage(OrderStatus status, Pageable pageable) {
        Page<Order> orders = orderRepository.findByStatus(status, pageable);
        // Carregar orderItems para cada pedido
        orders.getContent().forEach(order -> {
            if (order.getOrderItems() != null) {
                // Forçar carregamento dos itens
                order.getOrderItems().size();
                order.getOrderItems().forEach(item -> {
                    if (item.getProduct() != null) {
                        item.getProduct().getNome(); // Forçar carregamento do produto
                    }
                });
            }
        });
        return orders;
    }

    /**
     * Gerar número único do pedido
     */
    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Buscar pedido por ID com itens carregados
     */
    // Adicionar @Transactional(readOnly = true) nos métodos de busca
    @Transactional(readOnly = true)
    public Order findById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado: " + id));
        
        // Forçar carregamento dos itens (lazy loading)
        order.getOrderItems().size();
        
        return order;
    }
    
    @Transactional(readOnly = true)
    public Page<Order> findAllPage(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        
        // Forçar carregamento dos itens para todos os pedidos
        orders.getContent().forEach(order -> {
            if (order.getOrderItems() != null) {
                order.getOrderItems().size();
            }
        });
        
        return orders;
    }

    public Page<Order> findByCustomer(Long customerId, Pageable pageable) {
        // Para paginação, usar o método normal e carregar itens depois
        Page<Order> orders = orderRepository.findByCustomerId(customerId, pageable);
        // Carregar itens para cada pedido
        orders.getContent().forEach(order -> {
            if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
                Order fullOrder = orderRepository.findByIdWithItems(order.getId()).orElse(order);
                order.setOrderItems(fullOrder.getOrderItems());
            }
        });
        return orders;
    }

    /**
     * Buscar pedidos por vendedor com itens
     */
    public Page<Order> findBySeller(Long sellerId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByOrderItemsProductSellerId(sellerId, pageable);
        // Carregar itens para cada pedido
        orders.getContent().forEach(order -> {
            if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
                Order fullOrder = orderRepository.findByIdWithItems(order.getId()).orElse(order);
                order.setOrderItems(fullOrder.getOrderItems());
            }
        });
        return orders;
    }

    /**
     * Converter Order em OrderResponseDTO
     */
    private OrderResponseDTO convertToOrderResponseDTO(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setEnderecoEntrega(order.getEnderecoEntrega());
        dto.setCriadoEm(order.getCriadoEm());
        dto.setAtualizadoEm(order.getAtualizadoEm());

        dto.setCustomerId(order.getCustomer().getId());
        dto.setCustomerName(order.getCustomer().getName());
        dto.setCustomerEmail(order.getCustomer().getEmail());

        if (order.getTracking() != null) {
            dto.setObservacoes(order.getTracking().getObservacoes());
            dto.setCodigoRastreamento(order.getTracking().getCodigoRastreamento());
        }

        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            List<OrderItemResponseDTO> items = order.getOrderItems().stream()
                    .map(item -> {
                        OrderItemResponseDTO itemDTO = new OrderItemResponseDTO();
                        itemDTO.setId(item.getId());
                        itemDTO.setProductId(item.getProduct().getId());
                        itemDTO.setProductNome(item.getProduct().getNome());
                        itemDTO.setProductDescricao(item.getProduct().getDescricao());
                        itemDTO.setProductImagemUrl(item.getProduct().getImagemUrl());
                        itemDTO.setQuantidade(item.getQuantidade());
                        itemDTO.setPrecoUnitario(item.getPrecoUnitario());
                        itemDTO.setSubtotal(item.getSubtotal());
                        itemDTO.setOrderId(order.getId());
                        itemDTO.setOrderNumber(order.getOrderNumber());
                        return itemDTO;
                    }).toList();
            dto.setItems(items);
        }

        return dto;
    }

    // MANTER O MÉTODO updateStatus SIMPLES
    @Transactional
    public Order updateStatus(Long id, OrderStatus newStatus) {
        Order order = findById(id);
        OrderStatus oldStatus = order.getStatus();
        
        order.setStatus(newStatus);
        order.setAtualizadoEm(LocalDateTime.now());
        
        // Atualizar tracking
        if (order.getTracking() != null) {
            order.getTracking().setStatus(newStatus);
            order.getTracking().setAtualizadoEm(LocalDateTime.now());
        }
        
        Order savedOrder = orderRepository.save(order);
        System.out.println("Status do pedido " + id + " alterado de " + oldStatus + " para " + newStatus);
        
        return savedOrder;
    }
    
    // ADICIONAR MÉTODO updateStatus COM AUDITORIA (SOBRECARGA)
    @Transactional
    public Order updateStatus(Long id, OrderStatus newStatus, String changedBy, Long changedById, String reason) {
        Order order = updateStatus(id, newStatus);
        
        // Log da mudança
        System.out.println("Status atualizado por: " + changedBy + " (ID: " + changedById + "). Motivo: " + reason);
        
        return order;
    }

    /**
     * Cancelar pedido com auditoria
     */
    @Transactional
    public Order cancelOrder(Long orderId, String cancelledBy, Long userId, String reason) {
        Order order = findById(orderId);
        
        // Verificar se pode cancelar
        if (order.getStatus() == OrderStatus.ENTREGUE || order.getStatus() == OrderStatus.CANCELADO) {
            throw new RuntimeException("Pedidos entregues ou já cancelados não podem ser cancelados");
        }
        
        // Devolver estoque
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.aumentarEstoque(item.getQuantidade());
            productRepository.save(product);
        }
        
        // Atualizar status com auditoria
        Order cancelledOrder = updateStatus(orderId, OrderStatus.CANCELADO, cancelledBy, userId, reason);
        
        // Notificar sobre cancelamento
        if (notificationService != null) {
            notificationService.notifyOrderStatusChange(cancelledOrder, "Pedido cancelado: " + reason);
        }
        
        return cancelledOrder;
    }

    /**
     * Confirmar entrega do pedido
     */
    @Transactional
    public Order confirmDelivery(Long orderId) {
        Order order = findById(orderId);
        
        if (order.getStatus() != OrderStatus.ENVIADO) {
            throw new RuntimeException("Apenas pedidos enviados podem ser confirmados como entregues");
        }
        
        Order deliveredOrder = updateStatus(orderId, OrderStatus.ENTREGUE);
        
        // Notificar sobre entrega
        if (notificationService != null) {
            notificationService.notifyOrderStatusChange(deliveredOrder, "Pedido entregue e confirmado pelo cliente");
        }
        
        return deliveredOrder;
    }

    /**
     * Aprovar pedido (seller)
     */
    @Transactional
    public Order approveOrder(Long orderId, Long sellerId, String notes) {
        Order order = findById(orderId);
        
        if (order.getStatus() != OrderStatus.PENDENTE) {
            throw new RuntimeException("Apenas pedidos pendentes podem ser aprovados");
        }
        
        // Atualizar status para confirmado
        Order approvedOrder = updateStatus(orderId, OrderStatus.CONFIRMADO, "Seller", sellerId, notes);
        
        // Notificar sobre aprovação
        if (notificationService != null) {
            notificationService.notifyOrderStatusChange(approvedOrder, "Pedido aprovado pelo vendedor: " + notes);
        }
        
        return approvedOrder;
    }

    /**
     * Atualizar pedido
     */
    @Transactional
    public Order update(Long id, OrderUpdateDTO updateDTO) {
        Order order = findById(id);
        
        if (updateDTO.getEnderecoEntrega() != null) {
            order.setEnderecoEntrega(updateDTO.getEnderecoEntrega());
        }
        
        if (updateDTO.getObservacoes() != null && order.getTracking() != null) {
            order.getTracking().setObservacoes(updateDTO.getObservacoes());
        }
        
        order.setAtualizadoEm(LocalDateTime.now());
        return orderRepository.save(order);
    }

    /**
     * Buscar pedidos recentes
     */
    public List<Order> findRecentOrders() {
        return orderRepository.findTop10ByOrderByCriadoEmDesc();
    }

    public Optional<Order> findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    public void delete(Long id) {
        delete(id);
    }

    public Page<Order> findByStatus(OrderStatus status, Pageable pageable) {
        return findByStatus(status, pageable);
    }

    @Transactional
    public Tracking generateTrackingForExistingOrder(Long orderId) {
        Order order = findById(orderId);
        
        System.out.println("=== GERANDO TRACKING PARA PEDIDO " + orderId + " ===");
        
        // Verificar se já existe tracking
        if (order.getTracking() != null) {
            System.out.println("Tracking existe, código atual: " + order.getTracking().getCodigoRastreamento());
            
            if (order.getTracking().getCodigoRastreamento() == null || order.getTracking().getCodigoRastreamento().isEmpty()) {
                // Atualizar tracking existente com código
                String newCode = generateTrackingCode();
                order.getTracking().setCodigoRastreamento(newCode);
                order.getTracking().setAtualizadoEm(LocalDateTime.now());
                
                Tracking saved = trackingRepository.save(order.getTracking());
                System.out.println("Código gerado e salvo: " + newCode);
                return saved;
            } else {
                System.out.println("Código já existe: " + order.getTracking().getCodigoRastreamento());
                return order.getTracking();
            }
        } else {
            // Criar novo tracking
            System.out.println("Criando novo tracking...");
            Tracking tracking = new Tracking();
            tracking.setOrder(order);
            tracking.setStatus(order.getStatus());
            tracking.setObservacoes("Entregar no período da manhã");
            
            String newCode = generateTrackingCode();
            tracking.setCodigoRastreamento(newCode);
            tracking.setAtualizadoEm(LocalDateTime.now());
            
            Tracking saved = trackingRepository.save(tracking);
            System.out.println("Novo tracking criado com código: " + newCode);
            return saved;
        }
    }

    /**
     * Gerar código único de rastreamento
     */
    private String generateTrackingCode() {
        return "TRK-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Transactional
    public Tracking saveTracking(Tracking tracking) {
        System.out.println("Salvando tracking: " + tracking.getCodigoRastreamento());
        return trackingRepository.save(tracking);
    }

    // ADICIONAR ESTE MÉTODO NO OrderService
    @Transactional
    public void updateTotal(Long orderId, BigDecimal newTotal) {
        Order order = findById(orderId);
        order.setTotalAmount(newTotal);
        order.setAtualizadoEm(LocalDateTime.now());
        orderRepository.save(order);
        
        System.out.println("Total do pedido " + orderId + " atualizado para: " + newTotal);
    }

    public Page<Order> findBySellerAndStatus(Long sellerId, OrderStatus status, Pageable pageable) {
    System.out.println("Buscando pedidos do seller " + sellerId + " com status " + status);
    return orderRepository.findByOrderItemsProductSellerIdAndStatus(sellerId, status, pageable);
    }

    /**
     * Buscar pedidos por customer e status
     */
    public Page<Order> findByCustomerAndStatus(Long customerId, OrderStatus status, Pageable pageable) {
        return orderRepository.findByCustomerIdAndStatus(customerId, status, pageable);
    }
}

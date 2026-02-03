package com.StoreProject.services;

import com.StoreProject.model.*;
import com.StoreProject.cartdto.*;
import com.StoreProject.orderdto.OrderItemRequestDTO;
import com.StoreProject.orderdto.OrdersRequestDTO;
import com.StoreProject.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ShoppingCartService {
    
    @Autowired
    private ShoppingCartRepository cartRepository;
    
    @Autowired
    private CartItemRepository cartItemRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private OrderService orderService;
    
    /**
     * Obter ou criar carrinho do cliente
     */
    public ShoppingCart getOrCreateCart(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Cliente não encontrado: " + customerId));
        
        return cartRepository.findByCustomer(customer)
            .orElseGet(() -> {
                ShoppingCart newCart = new ShoppingCart();
                newCart.setCustomer(customer);
                return cartRepository.save(newCart);
            });
    }
    
    /**
     * Adicionar produto ao carrinho
     */
    @Transactional
    public CartItem addToCart(Long customerId, CartItemResponseDTO requestDTO) {
        ShoppingCart cart = getOrCreateCart(customerId);
        
        Product product = productRepository.findById(requestDTO.getProductId())
            .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + requestDTO.getProductId()));
        
        // Verificar se produto está disponível
        if (!product.isDisponivelParaVenda()) {
            throw new RuntimeException("Produto não está disponível para venda");
        }
        
        // Verificar estoque
        if (product.getEstoque() < requestDTO.getQuantidade()) {
            throw new RuntimeException("Estoque insuficiente. Disponível: " + product.getEstoque());
        }
        
        // Verificar se item já existe no carrinho
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);
        
        CartItem cartItem;
        if (existingItem.isPresent()) {
            // Atualizar quantidade
            cartItem = existingItem.get();
            int novaQuantidade = cartItem.getQuantidade() + requestDTO.getQuantidade();
            
            if (novaQuantidade > product.getEstoque()) {
                throw new RuntimeException("Quantidade total excede o estoque disponível");
            }
            
            cartItem.setQuantidade(novaQuantidade);
        } else {
            // Criar novo item
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantidade(requestDTO.getQuantidade());
            cartItem.setPrecoUnitario(product.getPrecoAtual());
        }
        
        cartItem = cartItemRepository.save(cartItem);
        updateCartTotals(cart);
        
        return cartItem;
    }
    
    /**
     * Atualizar quantidade de item no carrinho
     */
    @Transactional
    public CartItem updateCartItem(Long customerId, Long cartItemId, Integer novaQuantidade) {
        ShoppingCart cart = getOrCreateCart(customerId);
        
        CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new RuntimeException("Item do carrinho não encontrado: " + cartItemId));
        
        // Verificar se o item pertence ao carrinho do cliente
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Item não pertence ao carrinho do cliente");
        }
        
        // Verificar estoque
        if (cartItem.getProduct().getEstoque() < novaQuantidade) {
            throw new RuntimeException("Estoque insuficiente. Disponível: " + cartItem.getProduct().getEstoque());
        }
        
        cartItem.setQuantidade(novaQuantidade);
        cartItem = cartItemRepository.save(cartItem);
        updateCartTotals(cart);
        
        return cartItem;
    }
    
    /**
     * Remover item do carrinho
     */
    @Transactional
    public void removeFromCart(Long customerId, Long cartItemId) {
        ShoppingCart cart = getOrCreateCart(customerId);
        
        CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new RuntimeException("Item do carrinho não encontrado: " + cartItemId));
        
        // Verificar se o item pertence ao carrinho do cliente
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Item não pertence ao carrinho do cliente");
        }
        
        cartItemRepository.delete(cartItem);
        updateCartTotals(cart);
    }
    
    /**
     * Limpar carrinho
     */
    @Transactional
    public void clearCart(Long customerId) {
        ShoppingCart cart = getOrCreateCart(customerId);
        
        if (cart.getCartItems() != null && !cart.getCartItems().isEmpty()) {
            List<CartItem> itemsToDelete = new ArrayList<>(cart.getCartItems());
            for (CartItem item : itemsToDelete) {
                cartItemRepository.delete(item);
            }
            cart.getCartItems().clear();
        }
        
        cart.setTotalAmount(BigDecimal.ZERO);
        cart.setTotalItems(0);
        cart.setAtualizadoEm(LocalDateTime.now());
        
        cartRepository.save(cart);
    }
    
    /**
     * Obter carrinho do cliente
     */
    public ShoppingCart getCart(Long customerId) {
        return getOrCreateCart(customerId);
    }
    
    /**
     * Finalizar compra (checkout) - CORRIGIDO
     */
    @Transactional
    public Order checkout(Long customerId, CheckoutRequestDTO checkoutDTO) {
        ShoppingCart cart = getOrCreateCart(customerId);
        
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new RuntimeException("Carrinho está vazio");
        }
        
        List<CartItem> cartItemsCopy = new ArrayList<>(cart.getCartItems());
        
        // Verificar disponibilidade dos produtos
        for (CartItem item : cartItemsCopy) {
            Product product = item.getProduct();
            if (!product.isDisponivelParaVenda()) {
                throw new RuntimeException("Produto não disponível: " + product.getNome());
            }
            if (product.getEstoque() < item.getQuantidade()) {
                throw new RuntimeException("Estoque insuficiente para: " + product.getNome());
            }
        }
     
        Order order = createOrderFromCartItems(cart, cartItemsCopy, checkoutDTO);
        clearCart(customerId);
        
        return order;
    }
    
    /**
     * Criar pedido a partir dos itens do carrinho - NOVO MÉTODO
     */
    private Order createOrderFromCartItems(ShoppingCart cart, List<CartItem> cartItems, CheckoutRequestDTO checkoutDTO) {
        // Converter itens para OrdersRequestDTO
        OrdersRequestDTO orderDTO = new OrdersRequestDTO();
        orderDTO.setCustomerId(cart.getCustomer().getId());
        orderDTO.setEnderecoEntrega(checkoutDTO.getEnderecoEntrega());
        orderDTO.setObservacoes(checkoutDTO.getObservacoes());
        
        // Converter itens do carrinho para itens do pedido
        List<OrderItemRequestDTO> orderItems = cartItems.stream()
            .map(cartItem -> {
                OrderItemRequestDTO itemDTO = new OrderItemRequestDTO();
                itemDTO.setProductId(cartItem.getProduct().getId());
                itemDTO.setQuantidade(cartItem.getQuantidade());
                return itemDTO;
            })
            .toList();
        
        orderDTO.setItems(orderItems);
        
        return orderService.create(orderDTO);
    }
    
    /**
     * Atualizar totais do carrinho
     */
    private void updateCartTotals(ShoppingCart cart) {
        cart.calculateTotals();
        cartRepository.save(cart);
    }
    
    /**
     * Criar pedido a partir do carrinho
     */
    private Order createOrderFromCart(ShoppingCart cart, CheckoutRequestDTO checkoutDTO) {
        // Converter carrinho para OrdersRequestDTO
        OrdersRequestDTO orderDTO = new OrdersRequestDTO();
        orderDTO.setCustomerId(cart.getCustomer().getId());
        orderDTO.setEnderecoEntrega(checkoutDTO.getEnderecoEntrega());
        orderDTO.setObservacoes(checkoutDTO.getObservacoes());
        
        // Converter itens do carrinho para itens do pedido
        List<OrderItemRequestDTO> orderItems = cart.getCartItems().stream()
            .map(cartItem -> {
                OrderItemRequestDTO itemDTO = new OrderItemRequestDTO();
                itemDTO.setProductId(cartItem.getProduct().getId());
                itemDTO.setQuantidade(cartItem.getQuantidade());
                return itemDTO;
            })
            .toList();
        
        orderDTO.setItems(orderItems);
        
        return orderService.create(orderDTO);
    }
}
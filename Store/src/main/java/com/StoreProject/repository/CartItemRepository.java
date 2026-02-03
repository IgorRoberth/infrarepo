package com.StoreProject.repository;

import com.StoreProject.model.CartItem;
import com.StoreProject.model.ShoppingCart;
import com.StoreProject.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    // Buscar item específico no carrinho
    Optional<CartItem> findByCartAndProduct(ShoppingCart cart, Product product);

}
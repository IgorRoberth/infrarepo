package com.StoreProject.repository;

import com.StoreProject.model.ShoppingCart;
import com.StoreProject.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {

    // Buscar carrinho ativo do cliente
    Optional<ShoppingCart> findByCustomer(Customer customer);
}
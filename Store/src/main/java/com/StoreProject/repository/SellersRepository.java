package com.StoreProject.repository;

import com.StoreProject.model.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SellersRepository extends JpaRepository<Seller, Long> {
    
    Optional<Seller> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByCnpj(String cnpj);
    Optional<Seller> findByCnpjAndIdNot(String cnpj, Long id);

}

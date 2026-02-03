package com.StoreProject.repository;

import com.StoreProject.model.Customer;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByUsername(String username);
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByCpf(String cpf);
    List<Customer> findByAtivoTrue();
    
    // QUERY NATIVA PARA FORÇAR UPDATE DA SENHA
    @Modifying
    @Transactional
    @Query(value = "UPDATE customer SET password = :password WHERE id = :id", nativeQuery = true)
    void updatePasswordById(@Param("id") Long id, @Param("password") String password);
    
    // QUERY PARA VERIFICAR SENHA ATUAL
    @Query(value = "SELECT password FROM customer WHERE id = :id", nativeQuery = true)
    String getPasswordById(@Param("id") Long id);
}

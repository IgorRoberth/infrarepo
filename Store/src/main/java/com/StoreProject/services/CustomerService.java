package com.StoreProject.services;

import com.StoreProject.model.Customer;
import com.StoreProject.repository.CustomerRepository;
import com.StoreProject.securityconfig.JwtUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository repository;

    @PersistenceContext
    private EntityManager entityManager;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Autowired
    private JwtUtil jwtUtil;

    public Customer register(Customer customer) {
        if (repository.findByUsername(customer.getUsername()).isPresent()) {
            throw new RuntimeException("Username já está em uso");
        }
        if (repository.findByEmail(customer.getEmail()).isPresent()) {
            throw new RuntimeException("E-mail já está em uso");
        }
    
        if (customer.getCpf() != null && !customer.getCpf().trim().isEmpty()) {
            if (repository.findByCpf(customer.getCpf()).isPresent()) {
                throw new RuntimeException("CPF já está em uso");
            }
        }
    
        // GARANTIR que o campo ativo seja sempre definido
        if (customer.getAtivo() == null) {
            customer.setAtivo(true); // Padrão ativo
        }

        customer.setPassword(encoder.encode(customer.getPassword()));
        Customer saved = repository.save(customer);
        return saved;
    }

    public List<Customer> listFull() {
        return repository.findAll();
    }
    
    public Customer searchById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado: " + id));
    }

    @Transactional
    public Customer update(Long id, Customer customerUpdate) {
        Customer existingCustomer = searchById(id);

        if (customerUpdate.getCpf() != null && !customerUpdate.getCpf().trim().isEmpty()) {
            String newCpf = customerUpdate.getCpf().trim();
            // Só validar se o CPF está sendo alterado
            if (!newCpf.equals(existingCustomer.getCpf())) {
                Optional<Customer> userWithSameCpf = repository.findByCpf(newCpf);
                if (userWithSameCpf.isPresent() && !userWithSameCpf.get().getId().equals(id)) {
                    throw new RuntimeException("CPF já está em uso por outro cliente");
                }
                existingCustomer.setCpf(newCpf);
                System.out.println("CPF atualizado: " + newCpf);
            } else {
                System.out.println("CPF não alterado: " + newCpf);
            }
        }

        // Validações de username único
        if (customerUpdate.getUsername() != null && !existingCustomer.getUsername().equals(customerUpdate.getUsername())) {
            Optional<Customer> userWithSameUsername = repository.findByUsername(customerUpdate.getUsername());
            if (userWithSameUsername.isPresent() && !userWithSameUsername.get().getId().equals(id)) {
                throw new RuntimeException("Username já está em uso");
            }
            existingCustomer.setUsername(customerUpdate.getUsername());
            System.out.println("Username atualizado: " + customerUpdate.getUsername());
        }

        // Validações de email único
        if (customerUpdate.getEmail() != null && !existingCustomer.getEmail().equals(customerUpdate.getEmail())) {
            Optional<Customer> userWithSameEmail = repository.findByEmail(customerUpdate.getEmail());
            if (userWithSameEmail.isPresent() && !userWithSameEmail.get().getId().equals(id)) {
                throw new RuntimeException("E-mail já está em uso");
            }
            existingCustomer.setEmail(customerUpdate.getEmail());
            System.out.println("Email atualizado: " + customerUpdate.getEmail());
        }

        // Atualizar outros campos básicos
        if (customerUpdate.getName() != null) {
            existingCustomer.setName(customerUpdate.getName());
            System.out.println("Nome atualizado: " + customerUpdate.getName());
        }
        if (customerUpdate.getPhone() != null) {
            existingCustomer.setPhone(customerUpdate.getPhone());
        }
        if (customerUpdate.getEndereco() != null) {
            existingCustomer.setEndereco(customerUpdate.getEndereco());
        }
        if (customerUpdate.getCep() != null) {
            existingCustomer.setCep(customerUpdate.getCep());
        }
        if (customerUpdate.getCity() != null) {
            existingCustomer.setCity(customerUpdate.getCity());
        }
        if (customerUpdate.getEstado() != null) {
            existingCustomer.setEstado(customerUpdate.getEstado());
        }

        // Modificar a parte da senha no método update:
        if (customerUpdate.getPassword() != null && !customerUpdate.getPassword().trim().isEmpty()) {
            String newPassword = customerUpdate.getPassword().trim();
            System.out.println("Atualizando senha...");
            System.out.println("Nova senha recebida: " + newPassword);
            
            // VERIFICAR SE É HASH OU TEXTO PLANO
            if (newPassword.startsWith("$2a$") && newPassword.length() == 60) {
                if (newPassword.equals(existingCustomer.getPassword())) {
                } else {
                    existingCustomer.setPassword(newPassword);
                }
            } else {
                String encodedPassword = encoder.encode(newPassword);
                existingCustomer.setPassword(encodedPassword);
                // Teste imediato
                boolean testMatch = encoder.matches(newPassword, encodedPassword);
            }
        }

        // Salvar com flush e refresh
        try {
            Customer saved = repository.save(existingCustomer);
            
            if (entityManager != null) {
                entityManager.flush();
                entityManager.refresh(saved);
            }
            return saved;
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao atualizar customer: " + e.getMessage());
        }
    }

    // Adicionar este método específico para alterar senha
    
    @Transactional
    public Customer updatePasswordOnly(Long id, String newPassword) {

        Customer customer = searchById(id);
        // Criptografar a nova senha
        String encodedPassword = encoder.encode(newPassword.trim());
        // Definir a nova senha
        customer.setPassword(encodedPassword);
        Customer saved = repository.save(customer);

        if (entityManager != null) {
            entityManager.flush(); // Força commit
            entityManager.clear(); // Limpa cache
            
            // Buscar novamente do banco
            Customer refreshed = repository.findById(id).orElse(null);
            if (refreshed != null) {
                boolean testResult = encoder.matches(newPassword.trim(), refreshed.getPassword());
                if (!testResult) {
                    throw new RuntimeException("ERRO: Senha não foi salva corretamente no banco");
                }
                
                return refreshed;
            }
        }
        
        // Fallback se entityManager não estiver disponível
        boolean testResult = encoder.matches(newPassword.trim(), saved.getPassword());
        if (!testResult) {
            throw new RuntimeException("Erro ao salvar nova senha");
        }
        return saved;
    }

    // Para alterar status com persistência garantida
    @Transactional
    public Customer updateStatus(Long id, Boolean ativo) {

        Customer customer = searchById(id);
        System.out.println("Status antes: " + customer.getAtivo());
        customer.setAtivo(ativo);
        // Salvar, flush e refresh usando EntityManager
        Customer saved = repository.save(customer);
        entityManager.flush(); // Força commit imediato
        entityManager.refresh(saved); // Atualiza do banco
        return saved;
    }

    @Transactional
    public void excluded(Long id) {
        Customer customer = searchById(id);
        // EntityManager para flush
        entityManager.flush();
        repository.delete(customer);
        entityManager.flush();
        
        System.out.println("Customer deletado do banco");
        
        boolean exists = repository.existsById(id);
        System.out.println("Customer ainda existe no banco: " + exists);
    }

    public Optional<Customer> findByUsername(String username) {
        return repository.findByUsername(username);
    }

    // para usar EntityManager
    @Transactional
    public Customer reactivateCustomer(Long id) {
        Customer customer = searchById(id);
        customer.setAtivo(true);
        
        // Usar EntityManager para flush e refresh
        Customer updated = repository.save(customer);
        entityManager.flush();
        entityManager.refresh(updated);
        return updated;
    }

    @Transactional
    public Customer deactivateCustomer(Long id) {
        Customer customer = searchById(id);
        customer.setAtivo(false);

        // Usar EntityManager para flush e refresh
        Customer updated = repository.save(customer);
        entityManager.flush();
        entityManager.refresh(updated);
        return updated;
    }

    @Transactional
public Customer forceUpdatePassword(Long id, String newPassword) {

    // Buscar customer
    Customer customer = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Customer não encontrado: " + id));

    // Criptografar nova senha
    String encodedPassword = encoder.encode(newPassword.trim());
    System.out.println("Hash NOVO: " + encodedPassword);
    
    customer.setPassword(encodedPassword);
    Customer saved1 = repository.save(customer);

    if (entityManager != null) {
        entityManager.flush();
        entityManager.clear();
    }
    repository.updatePasswordById(id, encodedPassword);

    if (entityManager != null) {
        entityManager.flush();
    }

    Customer verification = repository.findById(id).orElse(null);
    if (verification != null) {
        System.out.println("Hash FINAL no banco: " + verification.getPassword());
        
        boolean testResult = encoder.matches(newPassword.trim(), verification.getPassword());
        if (!testResult) {
            throw new RuntimeException("ERRO CRÍTICO: Senha não foi salva no banco!");
        }
        
        return verification;
    }
    
    throw new RuntimeException("Erro ao verificar senha salva");
    }
}


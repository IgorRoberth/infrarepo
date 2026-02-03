package com.StoreProject.services;

import com.StoreProject.exceptions.CustomException;
import com.StoreProject.exceptions.ErrorCode;
import com.StoreProject.model.Seller;
import com.StoreProject.repository.SellersRepository;
import com.StoreProject.sellerdto.SellerUpdate;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ServiceSellers {

    @Autowired
    private SellersRepository sellersRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Seller save(Seller seller) {
        return sellersRepository.save(seller);
    }

    public Seller findById(Long id) {
        return sellersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Seller não encontrado: " + id));
    }

    public Seller findByEmail(String email) {
        Optional<Seller> seller = sellersRepository.findByEmail(email);
        return seller.orElse(null);
    }

    public List<Seller> listAll() {
        return sellersRepository.findAll();
    }

    private boolean findByCnpjAndIdNot(String cnpj, Long id) {
        return true;
    }

    @Transactional
    public Seller register(Seller seller) {
        if (findByEmail(seller.getEmail()) != null) {
            throw new CustomException(
                    ErrorCode.EMAIL_EXISTENTE.getCode(),
                    ErrorCode.EMAIL_EXISTENTE.getMessage()
            );
        }

        boolean cnpjExiste;
        if (seller.getId() == null) {
            // Novo cadastro: só checa se já existe o CNPJ
            cnpjExiste = sellersRepository.existsByCnpj(seller.getCnpj());
        } else {
            cnpjExiste = findByCnpjAndIdNot(seller.getCnpj(), seller.getId());
        }

        if (cnpjExiste) {
            throw new CustomException(
                    ErrorCode.CNPJ_EXISTENTE.getCode(),
                    ErrorCode.CNPJ_EXISTENTE.getMessage()
            );
        }

        if (!seller.getPassword().startsWith("$2a$")) {
            seller.setPassword(passwordEncoder.encode(seller.getPassword()));
        }

        seller.setAtivo(true);
        seller.setCriadoEm(LocalDateTime.now());

        return sellersRepository.save(seller);
    }



    @Transactional
    public Seller update(Long id, Object updateDTO) {
        Seller seller = findById(id);

        // VERIFICAR SE É SellerUpdate
        if (updateDTO instanceof SellerUpdate) {
            SellerUpdate sellerUpdate = (SellerUpdate) updateDTO;

            // ATUALIZAR TODOS OS CAMPOS
            if (sellerUpdate.getNome() != null && !sellerUpdate.getNome().trim().isEmpty()) {
                seller.setNome(sellerUpdate.getNome().trim());
                System.out.println("Atualizando nome: " + sellerUpdate.getNome());
            }

            if (sellerUpdate.getEmail() != null && !sellerUpdate.getEmail().trim().isEmpty()) {
                // Verificar se email já existe (exceto o próprio seller)
                Seller existingWithEmail = findByEmail(sellerUpdate.getEmail());
                if (existingWithEmail != null && !existingWithEmail.getId().equals(id)) {
                    throw new RuntimeException("Email já está em uso por outro seller");
                }
                seller.setEmail(sellerUpdate.getEmail().trim());
                System.out.println("Atualizando email: " + sellerUpdate.getEmail());
            }

            if (sellerUpdate.getTelefone() != null && !sellerUpdate.getTelefone().trim().isEmpty()) {
                seller.setTelefone(sellerUpdate.getTelefone().trim());
            }

            if (sellerUpdate.getEndereco() != null && !sellerUpdate.getEndereco().trim().isEmpty()) {
                seller.setEndereco(sellerUpdate.getEndereco().trim());
            }

            if (sellerUpdate.getCnpj() != null && !sellerUpdate.getCnpj().trim().isEmpty()) {
                seller.setCnpj(sellerUpdate.getCnpj().trim());
            }

            if (sellerUpdate.getRazaoSocial() != null && !sellerUpdate.getRazaoSocial().trim().isEmpty()) {
                seller.setRazaoSocial(sellerUpdate.getRazaoSocial().trim());
            }

            if (sellerUpdate.getCep() != null && !sellerUpdate.getCep().trim().isEmpty()) {
                seller.setCep(sellerUpdate.getCep().trim());
            }

            if (sellerUpdate.getEstado() != null && !sellerUpdate.getEstado().trim().isEmpty()) {
                seller.setEstado(sellerUpdate.getEstado().trim());
            }

            if (sellerUpdate.getCidade() != null && !sellerUpdate.getCidade().trim().isEmpty()) {
                seller.setCidade(sellerUpdate.getCidade().trim());
            }

            if (sellerUpdate.getPassword() != null && !sellerUpdate.getPassword().trim().isEmpty()) {
                String newPassword = sellerUpdate.getPassword().trim();
                if (!newPassword.startsWith("$2a$")) {
                    seller.setPassword(passwordEncoder.encode(newPassword));
                } else {
                    seller.setPassword(newPassword);
                }
            }

            // DEFINIR DATA DE ATUALIZAÇÃO
            seller.setCriadoEm(LocalDateTime.now());
        }

        // SALVAR E RETORNAR
        Seller updatedSeller = sellersRepository.save(seller);
        System.out.println("Seller atualizado: " + updatedSeller.getNome() + " - " + updatedSeller.getEmail());
        return updatedSeller;
    }

    @Transactional
    public void excluded(Long id) {
        Seller seller = findById(id);
        sellersRepository.delete(seller);
    }

    public boolean existsByEmail(String email) {
        return sellersRepository.existsByEmail(email);
    }

    public Long count() {
        return sellersRepository.count();
    }
}
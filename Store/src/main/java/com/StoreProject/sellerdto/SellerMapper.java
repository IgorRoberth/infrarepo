package com.StoreProject.sellerdto;

import com.StoreProject.model.Seller;
import org.springframework.stereotype.Component;

@Component
public class SellerMapper {

    public Seller toEntity(SellerRequest dto) {
        Seller seller = new Seller();
        seller.setNome(dto.getNome());
        seller.setEmail(dto.getEmail());
        seller.setPassword(dto.getPassword());
        seller.setTelefone(dto.getTelefone());
        seller.setEndereco(dto.getEndereco());
        seller.setCnpj(dto.getCnpj());
        seller.setRazaoSocial(dto.getRazaoSocial());
        seller.setCep(dto.getCep());
        seller.setEstado(dto.getEstado());
        seller.setCidade(dto.getCidade());
        
        return seller;
    }

    public SellerResponse toResponseDTO(Seller seller) {
        SellerResponse dto = new SellerResponse();
        dto.setId(seller.getId());
        dto.setNome(seller.getNome());
        dto.setEmail(seller.getEmail());
        dto.setTelefone(seller.getTelefone());
        dto.setEndereco(seller.getEndereco());
        dto.setCnpj(seller.getCnpj());                   
        dto.setRazaoSocial(seller.getRazaoSocial());     
        dto.setCep(seller.getCep());                     
        dto.setEstado(seller.getEstado());               
        dto.setCidade(seller.getCidade());               
        dto.setCriadoEm(seller.getCriadoEm());
        dto.setAtivo(seller.getAtivo());
        return dto;
    }

    public void updateEntityFromDTO(Seller seller, SellerUpdate dto) {
        if (dto.getNome() != null) seller.setNome(dto.getNome());
        if (dto.getEmail() != null) seller.setEmail(dto.getEmail());
        if (dto.getTelefone() != null) seller.setTelefone(dto.getTelefone());
        if (dto.getEndereco() != null) seller.setEndereco(dto.getEndereco());
        if (dto.getCnpj() != null) seller.setCnpj(dto.getCnpj());
        if (dto.getRazaoSocial() != null) seller.setRazaoSocial(dto.getRazaoSocial());
        if (dto.getCep() != null) seller.setCep(dto.getCep());
        if (dto.getEstado() != null) seller.setEstado(dto.getEstado());
        if (dto.getCidade() != null) seller.setCidade(dto.getCidade());
    }
}
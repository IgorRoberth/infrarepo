package com.StoreProject.produtctdto;

import com.StoreProject.enums.ProductStatus;
import com.StoreProject.model.Product;
import com.StoreProject.model.Seller;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class ProductMapper {

    public Product toEntity(ProductRequestDTO dto, Seller seller) {
        Product product = new Product();
        // Campos básicos
        product.setNome(dto.getNome());
        product.setDescricao(dto.getDescricao());
        product.setPreco(dto.getPreco());
        product.setEstoque(dto.getEstoque());
        product.setCategoria(dto.getCategoria());
        product.setMarca(dto.getMarca());
        product.setImagemUrl(dto.getImagemUrl());
        // Novos campos
        product.setSku(dto.getSku());
        product.setPeso(dto.getPeso());
        product.setAltura(dto.getAltura());
        product.setLargura(dto.getLargura());
        product.setProfundidade(dto.getProfundidade());
        product.setFabricante(dto.getFabricante());
        product.setStatus(dto.getStatus() != null ? dto.getStatus() : ProductStatus.ATIVO);
        // Promoção
        product.setPrecoPromocional(dto.getPrecoPromocional());
        product.setPromocaoInicio(dto.getPromocaoInicio());
        product.setPromocaoFim(dto.getPromocaoFim());
        product.setPercentualDesconto(dto.getPercentualDesconto());
        // Relacionamento e padrões
        product.setSeller(seller);
        product.setAtivo(true);
        product.setCriadoEm(LocalDateTime.now());
        product.setNotaMedia(BigDecimal.ZERO);
        product.setTotalAvaliacoes(0);
        return product;
    }

    public ProductResponseDTO toResponseDTO(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setNome(product.getNome());
        dto.setDescricao(product.getDescricao());
        dto.setPreco(product.getPreco());
        dto.setEstoque(product.getEstoque());
        dto.setCategoria(product.getCategoria());
        dto.setMarca(product.getMarca());
        dto.setImagemUrl(product.getImagemUrl());
        dto.setSku(product.getSku());
        dto.setPeso(product.getPeso());
        dto.setAltura(product.getAltura());
        dto.setLargura(product.getLargura());
        dto.setProfundidade(product.getProfundidade());
        dto.setFabricante(product.getFabricante());
        dto.setStatus(product.getStatus());
        dto.setPrecoPromocional(product.getPrecoPromocional());
        dto.setPromocaoInicio(product.getPromocaoInicio());
        dto.setPromocaoFim(product.getPromocaoFim());
        dto.setPercentualDesconto(product.getPercentualDesconto());
        dto.setNotaMedia(product.getNotaMedia());
        dto.setTotalAvaliacoes(product.getTotalAvaliacoes());
        dto.setAtivo(product.getAtivo());
        dto.setCriadoEm(product.getCriadoEm());
        dto.setAtualizadoEm(product.getAtualizadoEm());
        
        // Campos calculados
        dto.setEmPromocao(product.isEmPromocao());
        dto.setPrecoAtual(product.getPrecoAtual());
        dto.setDisponivelParaVenda(product.isDisponivelParaVenda());
        dto.setTemEstoque(product.temEstoque());
        
        // Informações do vendedor
        if (product.getSeller() != null) {
            dto.setSellerId(product.getSeller().getId());
            dto.setSellerNome(product.getSeller().getNome());
            dto.setSellerEmail(product.getSeller().getEmail());
        }
        
        return dto;
    }

    // CORRIGIR MÉTODO updateEntityFromDTO
    public void updateEntityFromDTO(Product product, ProductUpdateDTO dto) {
        if (dto.getNome() != null) product.setNome(dto.getNome());
        if (dto.getDescricao() != null) product.setDescricao(dto.getDescricao());
        if (dto.getPreco() != null) product.setPreco(dto.getPreco());
        if (dto.getEstoque() != null) product.setEstoque(dto.getEstoque());
        if (dto.getCategoria() != null) product.setCategoria(dto.getCategoria());
        if (dto.getMarca() != null) product.setMarca(dto.getMarca());
        if (dto.getImagemUrl() != null) product.setImagemUrl(dto.getImagemUrl());
        // CORRIGIR - USAR OS VALORES DO DTO
        if (dto.getSku() != null) product.setSku(dto.getSku());
        if (dto.getPeso() != null) product.setPeso(dto.getPeso());
        if (dto.getAltura() != null) product.setAltura(dto.getAltura());
        if (dto.getLargura() != null) product.setLargura(dto.getLargura());
        if (dto.getProfundidade() != null) product.setProfundidade(dto.getProfundidade());
        if (dto.getFabricante() != null) product.setFabricante(dto.getFabricante());
        if (dto.getStatus() != null) product.setStatus(dto.getStatus());
        // Promoção
        if (dto.getPrecoPromocional() != null) product.setPrecoPromocional(dto.getPrecoPromocional());
        if (dto.getPromocaoInicio() != null) product.setPromocaoInicio(dto.getPromocaoInicio());
        if (dto.getPromocaoFim() != null) product.setPromocaoFim(dto.getPromocaoFim());
        if (dto.getPercentualDesconto() != null) product.setPercentualDesconto(dto.getPercentualDesconto());
        
        // Atualizar timestamp
        product.setAtualizadoEm(LocalDateTime.now());
    }
}
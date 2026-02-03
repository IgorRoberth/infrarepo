package com.StoreProject.utils;

import com.StoreProject.model.Product;
import com.StoreProject.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Component
public class ImageMigrationUtil {
    
    @Autowired
    private ProductRepository productRepository;
    
    private final String webImageDir = "src/main/resources/static/uploads/images/";
    
    public void migrateAllImages() {
        List<Product> products = productRepository.findAll();
        
        for (Product product : products) {
            try {
                migrateProductImage(product);
            } catch (Exception e) {
                System.err.println("Erro ao migrar imagem do produto " + product.getId() + ": " + e.getMessage());
            }
        }
    }
    
    private void migrateProductImage(Product product) throws IOException {
        String currentImagePath = product.getImagemUrl();
        
        if (currentImagePath == null || currentImagePath.isEmpty()) {
            return;
        }
        
        // Se já é um caminho relativo, não precisa migrar
        if (!currentImagePath.contains(":") && !currentImagePath.startsWith("/home/") && !currentImagePath.startsWith("/Users/")) {
            return;
        }
        
        Path sourcePath = Paths.get(currentImagePath);
        
        if (!Files.exists(sourcePath)) {
            System.err.println("Arquivo não encontrado: " + currentImagePath);
            return;
        }
        
        // Criar nome único para evitar conflitos
        String fileExtension = getFileExtension(sourcePath.getFileName().toString());
        String newFileName = "product_" + product.getId() + "_" + UUID.randomUUID().toString().substring(0, 8) + fileExtension;
        
        // Criar diretório se não existir
        Path webImageDirPath = Paths.get(webImageDir);
        Files.createDirectories(webImageDirPath);
        
        // Copiar arquivo
        Path destinationPath = webImageDirPath.resolve(newFileName);
        Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
        
        // Atualizar produto no banco
        product.setImagemUrl("uploads/images/" + newFileName);
        productRepository.save(product);
        
        System.out.println("Imagem migrada: " + product.getId() + " -> " + newFileName);
    }
    
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return (lastDotIndex == -1) ? "" : filename.substring(lastDotIndex);
    }
}
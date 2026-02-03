package com.StoreProject.controllers;

import com.StoreProject.services.ProductService;
import com.StoreProject.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "*")
public class ImageController {

    @Autowired
    private ProductService productService;

    private final String uploadDir = "src/main/resources/static/uploads/images/";

    /**
     * Servir imagem de produto por ID (CORRIGIDO)
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<Resource> getProductImage(@PathVariable Long productId) {
        try {
            Product product = productService.findById(productId);
            
            if (product.getImagemUrl() == null || product.getImagemUrl().isEmpty()) {
                System.err.println("Produto " + productId + " não tem imagemUrl definida");
                return ResponseEntity.notFound().build();
            }
            
            String imagePath = product.getImagemUrl();
            System.out.println("Buscando imagem para produto " + productId + ": " + imagePath);
            
            // VERIFICAR SE É CAMINHO ABSOLUTO
            if (isAbsolutePath(imagePath)) {
                System.out.println("Caminho absoluto detectado: " + imagePath);
                return serveAbsoluteFile(imagePath);
            } else {
                System.out.println("Caminho relativo detectado: " + imagePath);
                return serveRelativeFile(imagePath);
            }
            
        } catch (Exception e) {
            System.err.println("Erro ao servir imagem do produto " + productId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Verificar se é caminho absoluto
     */
    private boolean isAbsolutePath(String path) {
        return path.contains(":") || // Windows (C:)
               path.startsWith("/home/") || // Linux
               path.startsWith("/Users/") || // macOS
               path.startsWith("/"); // Unix-like
    }
    
    /**
     * Servir arquivo de caminho absoluto
     */
    private ResponseEntity<Resource> serveAbsoluteFile(String absolutePath) {
        try {
            Path filePath = Paths.get(absolutePath);
            
            if (!Files.exists(filePath)) {
                System.err.println("Arquivo não encontrado no caminho absoluto: " + absolutePath);
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(filePath);
            
            // Detectar tipo de conteúdo
            String contentType = detectContentType(filePath);
            
            System.out.println("Servindo arquivo absoluto: " + absolutePath + " (" + contentType + ")");
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                    .body(resource);
                    
        } catch (Exception e) {
            System.err.println("Erro ao servir arquivo absoluto: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Servir arquivo de caminho relativo (uploads)
     */
    private ResponseEntity<Resource> serveRelativeFile(String relativePath) {
        try {
            // Extrair apenas o nome do arquivo se for um caminho
            String filename = Paths.get(relativePath).getFileName().toString();
            Path filePath = Paths.get(uploadDir, filename);
            
            if (!Files.exists(filePath)) {
                System.err.println("Arquivo não encontrado no diretório de uploads: " + filePath);
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(filePath);
            String contentType = detectContentType(filePath);
            
            System.out.println("Servindo arquivo relativo: " + filePath + " (" + contentType + ")");
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                    .body(resource);
                    
        } catch (Exception e) {
            System.err.println("Erro ao servir arquivo relativo: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Detectar tipo de conteúdo do arquivo
     */
    private String detectContentType(Path filePath) {
        try {
            String contentType = Files.probeContentType(filePath);
            if (contentType != null) {
                System.out.println("Content-Type detectado automaticamente: " + contentType);
                return contentType;
            }
        } catch (Exception e) {
            System.err.println("Erro ao detectar content type: " + e.getMessage());
        }
        
        // Fallback baseado na extensão
        String filename = filePath.getFileName().toString().toLowerCase();
        String detectedType;
        
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            detectedType = "image/jpeg";
        } else if (filename.endsWith(".png")) {
            detectedType = "image/png";
        } else if (filename.endsWith(".webp")) {
            detectedType = "image/webp";
        } else if (filename.endsWith(".avif")) {
            detectedType = "image/avif";
        } else if (filename.endsWith(".gif")) {
            detectedType = "image/gif";
        } else if (filename.endsWith(".svg")) {
            detectedType = "image/svg+xml";
        } else if (filename.endsWith(".bmp")) {
            detectedType = "image/bmp";
        } else if (filename.endsWith(".tiff") || filename.endsWith(".tif")) {
            detectedType = "image/tiff";
        } else {
            detectedType = "application/octet-stream";
        }
        
        System.out.println("Content-Type detectado por extensão: " + detectedType + " para arquivo: " + filename);
        return detectedType;
    }
    
    /**
     * Endpoint para debug - verificar se imagem existe
     */
    @GetMapping("/product/{productId}/debug")
    public ResponseEntity<Map<String, Object>> debugProductImage(@PathVariable Long productId) {
        try {
            Product product = productService.findById(productId);
            String imagePath = product.getImagemUrl();
            
            boolean exists = false;
            String resolvedPath = "";
            String type = "";
            
            if (imagePath != null && !imagePath.isEmpty()) {
                if (isAbsolutePath(imagePath)) {
                    resolvedPath = imagePath;
                    type = "absolute";
                    exists = Files.exists(Paths.get(imagePath));
                } else {
                    String filename = Paths.get(imagePath).getFileName().toString();
                    resolvedPath = Paths.get(uploadDir, filename).toString();
                    type = "relative";
                    exists = Files.exists(Paths.get(uploadDir, filename));
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "productId", productId,
                "originalPath", imagePath != null ? imagePath : "null",
                "resolvedPath", resolvedPath,
                "type", type,
                "exists", exists,
                "uploadDir", uploadDir
            ));
            
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "productId", productId,
                "error", e.getMessage(),
                "exists", false
            ));
        }
    }
}

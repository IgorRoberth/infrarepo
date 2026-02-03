package com.StoreProject.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*")
public class FileUploadController {

    // Pasta onde as imagens serão salvas
    private final String uploadDir = "src/main/resources/static/uploads/images/";

    /**
     * Upload de imagem de produto
     */
    @PostMapping("/product-image")
    public ResponseEntity<?> uploadProductImage(@RequestParam("file") MultipartFile file) {
        try {
            // Validar se é uma imagem
            if (!isValidImageFile(file)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erro", "Arquivo deve ser uma imagem (JPG, PNG, WEBP, GIF)"));
            }

            // Validar tamanho (máximo 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erro", "Arquivo deve ter no máximo 5MB"));
            }

            // Gerar nome único para o arquivo
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID().toString() + "." + fileExtension;

            // Criar diretório se não existir
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Salvar arquivo
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Retornar informações do arquivo
            return ResponseEntity.ok(Map.of(
                "success", true,
                "filename", uniqueFilename,
                "originalName", originalFilename,
                "size", file.getSize(),
                "url", "/api/images/file/" + uniqueFilename,
                "path", "uploads/images/" + uniqueFilename
            ));

        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Erro ao salvar arquivo: " + e.getMessage()));
        }
    }

    /**
     * Validar se é um arquivo de imagem válido
     */
    private boolean isValidImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (
            contentType.equals("image/jpeg") ||
            contentType.equals("image/jpg") ||
            contentType.equals("image/png") ||
            contentType.equals("image/webp") ||
            contentType.equals("image/gif")
        );
    }

    /**
     * Obter extensão do arquivo
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "jpg"; // padrão
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    /**
     * Listar imagens disponíveis
     */
    @GetMapping("/images")
    public ResponseEntity<?> listImages() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                return ResponseEntity.ok(Map.of("images", new String[0]));
            }

            String[] images = new File(uploadDir).list((dir, name) -> 
                name.toLowerCase().matches(".*\\.(jpg|jpeg|png|webp|gif)$")
            );

            return ResponseEntity.ok(Map.of("images", images != null ? images : new String[0]));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Erro ao listar imagens: " + e.getMessage()));
        }
    }
}
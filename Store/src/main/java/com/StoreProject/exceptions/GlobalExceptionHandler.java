package com.StoreProject.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleNoResourceFoundException(NoResourceFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    /**
     * @param ex
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        System.out.println("GlobalExceptionHandler: capturando erro de validação");
        
        BindingResult result = ex.getBindingResult();
        
        // Pega a primeira mensagem de erro disponível
        String errorMessage = result.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("Erro de validação");

        String codigoErro = "E000"; 
        for (ErrorCode code : ErrorCode.values()) {
        if (errorMessage.contains("Razão social") && code == ErrorCode.INVALID_SOCIAL_REASON) {
        codigoErro = code.getCode();
        break;
    }
    if (code.getMessage().trim().equalsIgnoreCase(errorMessage.trim())) {
        codigoErro = code.getCode();
        break;
    }
}

        Map<String, Object> response = new HashMap<>();
        response.put("tipo", "VALIDATION_ERROR");
        response.put("erro", errorMessage);
        response.put("codigo", codigoErro);
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, Object>> handleCustomException(CustomException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("tipo", "BUSINESS_ERROR");
        response.put("erro", ex.getMessage());
        response.put("codigo", ex.getErrorCode());
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        System.out.println("GlobalExceptionHandler: erro genérico capturado: " + ex.getMessage());
        ex.printStackTrace();
        
        Map<String, Object> response = new HashMap<>();
        response.put("tipo", "INTERNAL_ERROR");
        response.put("erro", "Ocorreu um erro interno no servidor.");
        // Não retornar ex.getMessage() aqui para evitar vazamento de info técnica
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

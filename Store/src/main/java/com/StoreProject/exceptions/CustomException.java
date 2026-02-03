package com.StoreProject.exceptions;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class CustomException extends RuntimeException {

    private final String errorCode;
    private final String errorMessage;
    private final Map<String, String> details;

    // Construtor simples (sem detalhes extras)
    public CustomException(String errorCode, String errorMessage) {
        super(errorMessage); // <-- aqui define a mensagem na superclasse
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.details = null;
    }

    // Construtor com detalhes adicionais
    public CustomException(String errorCode, String errorMessage, Map<String, String> details) {
        super(errorMessage); // <-- aqui também
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.details = details;
    }
}

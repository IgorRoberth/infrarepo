package com.StoreProject.exceptions;

public enum ErrorCode {

    INVALID_NAME("E001", "Nome deve ter entre 2 e 100 caracteres"),
    INVALID_EMAIL("E002", "Email deve ter um formato válido"),
    INVALID_PASSWORD("E003", "Senha deve ter entre 6 e 100 caracteres"),
    INVALID_PHONE("E004", "Telefone deve ter um formato válido"),
    INVALID_ADDRESS("E005", "Endereço deve ter no máximo 200 caracteres"),
    INVALID_CNPJ("E006", "CNPJ deve conter exatamente 14 dígitos"),
    INVALID_SOCIAL_REASON("E007", "Razão social deve conter no máximo 150 caracteres"),
    INVALID_CEP("E008", "CEP deve ter formato válido"),
    INVALID_STATE("E009", "Estado deve conter 2 caracteres"),
    INVALID_CITY("E010", "Cidade deve conter no máximo 100 caracteres"),
    SELLER_NOT_FOUND("E011", "Vendedor não encontrado"),
    SELLER_REGISTRATION_FAILED("E012", "Falha ao registrar vendedor"),
    SELLER_UPDATE_FAILED("E013", "Falha ao atualizar vendedor"),
    INTERNAL_SERVER_ERROR("E014", "Erro interno no servidor"),
    SENHA_INCORRETA("E015", "Senha atual inválida."),
    LOGIN_ERROR("E016", "E-mail inválido."),
    CUSTOMER_NOT_FOUND("E017", "Cliente não encontrado"),
    CUSTOMER_DISABLED("E018", "Conta desativada"),
    PASSWORD_RESET_ERROR("E019", "Erro ao tentar recuperar senha"),
    AUTHORIZATION_ERROR("E020", "Token de autenticação necessário"),
    PASSWORD_UPDATE_ERROR("E021", "Erro ao atualizar a senha"),
    CNPJ_EXISTENTE("E022", "Já existe um vendedor cadastrado com este CNPJ"),
    EMAIL_EXISTENTE("E023", "E-mail existente"),

    INVALID_TOKEN("E024", "Token inválido ou expirado"),
    EXPIRED_TOKEN("E025", "Token expirado"),
    INVALID_USER_TYPE("E026", "Tipo de usuário inválido"),
    CURRENT_PASSWORD_REQUIRED("E027", "Senha atual é obrigatória quando não há token"),
    INVALID_CURRENT_PASSWORD("E028", "Senha atual incorreta"),
    PASSWORD_VERIFICATION_FAILED("E029", "Erro na verificação da nova senha"),
    SELLER_PROFILE_FETCH_FAILED("E030", "Erro ao buscar dados do vendedor"),
    AUTH_TOKEN_REQUIRED("E031", "Token de autenticação necessário"),
    ACCESS_FORBIDDEN("E032", "Acesso proibido, somente vendedores podem acessar esse recurso"),
    SELLER_STATUS_UPDATE_FAILED("E033", "Falha ao atualizar status do vendedor"),
    CADASTRO_CONCLUIDO_COM_SUCESSO("E034", "Produto cadastrado com sucesso."),
    PRODUTO_ATUALIZADO_COM_SUCESSO("E035", "Produto atualizado com sucesso."),
    ERROR_NO_SERVER("E036", "Ocorreu um erro interno no servidor."),
    NOME_CARACTERES("E037","Nome com formato inválido");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }

    public static String getMessageByCode(String code) {
        for (ErrorCode ec : values()) {
            if (ec.code.equals(code)) return ec.message;
        }
        return "Erro desconhecido";
    }
}
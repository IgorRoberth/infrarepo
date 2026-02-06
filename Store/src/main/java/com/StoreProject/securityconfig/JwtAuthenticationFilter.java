package com.StoreProject.securityconfig;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Filtro de autenticação JWT para validar tokens em cada requisição.
 * Melhorado para garantir a integridade da sessão e suporte aos headers de segurança.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * 
     * Mesmo ignorando o filtro, o SecurityConfig ainda aplicará os headers de segurança (CSP, HSTS, etc).
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();

        // Ignora requisições de pré-verificação
        if ("OPTIONS".equalsIgnoreCase(method)) return true;

        // Cadastro de vendedor é público
        if (path.equals("/api/registerseller") && "POST".equalsIgnoreCase(method)) return true;
        if (path.startsWith("/api/registerseller/") && "POST".equalsIgnoreCase(method)) return true;

        // Rotas de login e recursos estáticos
        return path.equals("/api/auth/seller/login") ||
               path.equals("/api/auth/customer/login") ||
               path.startsWith("/public/") ||
               path.startsWith("/home") ||
               path.startsWith("/css/") ||
               path.startsWith("/js/") ||
               path.endsWith(".html") ||
               path.equals("/error");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                if (jwtUtil.tokenValido(token)) {
                    String username = jwtUtil.getUsernameDoToken(token);
                    String userType = jwtUtil.getUserTypeFromToken(token);
                    Long userId = jwtUtil.getUserIdFromToken(token);

                    AuthenticatedUser authenticatedUser = new AuthenticatedUser(userId, username, userType);

                    // Garante que o tipo de usuário tenha o prefixo ROLE_ para o Spring Security
                    String roleName = userType.toUpperCase().startsWith("ROLE_") ? 
                                      userType.toUpperCase() : "ROLE_" + userType.toUpperCase();

                    JwtAuthentication authToken = new JwtAuthentication(
                            authenticatedUser, 
                            null, 
                            List.of(new SimpleGrantedAuthority(roleName)),
                            userId, 
                            userType
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // Define a autenticação no contexto do Spring
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                // SEGURANÇA: Limpa qualquer contexto residual em caso de erro no token
                SecurityContextHolder.clearContext();
                log.error("Falha na autenticação via Token: {}", e.getMessage());
            }
        }

        // Continua a cadeia de filtros (onde os Headers de segurança do SecurityConfig serão injetados)
        filterChain.doFilter(request, response);
    }

    /**
     * Classe interna para representar a autenticação customizada com ID e Tipo.
     */
    public static class JwtAuthentication extends UsernamePasswordAuthenticationToken {
    private final Long userId;
    private final String userType;

    public JwtAuthentication(Object principal, Object credentials, 
                             List<SimpleGrantedAuthority> authorities, Long userId, String userType) {
        super(principal, credentials, authorities);
        this.userId = userId;
        this.userType = userType;
    }

    public Long getUserId() { return userId; }
    public String getUserType() { return userType; }

    // Sobrescrever o equals para incluir os novos campos
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;       
        if (o == null || getClass() != o.getClass()) return false;
        // Verifica a igualdade na classe pai (UsernamePasswordAuthenticationToken)
        if (!super.equals(o)) return false;
        // Compara os campos específicos da subclasse
        JwtAuthentication that = (JwtAuthentication) o;
        return Objects.equals(userId, that.userId) && 
               Objects.equals(userType, that.userType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), userId, userType);
     }
   }
}
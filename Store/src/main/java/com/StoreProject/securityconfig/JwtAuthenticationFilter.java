package com.StoreProject.securityconfig;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
       String path = request.getServletPath();
       String method = request.getMethod();

    if ("OPTIONS".equalsIgnoreCase(method)) return true;

    if ("POST".equalsIgnoreCase(method) && path.equals("/api/registerseller")) return true;
    if (path.startsWith("/api/registerseller/") && "POST".equalsIgnoreCase(method)) return true;

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

        if (authHeader!= null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                if (jwtUtil.tokenValido(token)) {
                    String username = jwtUtil.getUsernameDoToken(token);
                    String userType = jwtUtil.getUserTypeFromToken(token);
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    AuthenticatedUser authenticatedUser = new AuthenticatedUser(userId, username, userType);

                    JwtAuthentication authToken = new JwtAuthentication(
                            authenticatedUser, 
                            null, 
                            List.of(new SimpleGrantedAuthority("ROLE_" + userType)),
                            userId, 
                            userType
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                logger.error("Falha na autenticação via Token: " + e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }

    public static class JwtAuthentication extends UsernamePasswordAuthenticationToken {
        private final Long userId;
        private final String userType;

        public JwtAuthentication(Object principal, Object credentials, List<SimpleGrantedAuthority> authorities, Long userId, String userType) {
            super(principal, credentials, authorities);
            this.userId = userId;
            this.userType = userType;
        }
        public Long getUserId() { return userId; }
        public String getUserType() { return userType; }
    }
}
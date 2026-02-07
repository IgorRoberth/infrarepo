package com.StoreProject.securityconfig;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.function.Function;
import java.util.logging.Logger;

import javax.crypto.SecretKey;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.expiration}")
    private long EXPIRATION_TIME;

    Logger logger = Logger.getLogger(getClass().getName());

    private SecretKey getSigningKey() {
        byte[] keyBytes = SECRET_KEY.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }


    /**
     * Gerar token para Customer
     */
    public String generateTokenForCustomer(String username, Long customerId) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", customerId)
                .claim("userType", "CUSTOMER")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Gerar token para Seller
     */
    public String generateTokenForSeller(String email, Long sellerId) {
        return Jwts.builder()
                .setSubject(email)
                .claim("userId", sellerId)
                .claim("userType", "SELLER")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Gerar token genérico
     */
    public String generateToken(String usernameOrEmail, String userType, Long userId) {
        return Jwts.builder()
                .setSubject(usernameOrEmail)
                .claim("userId", userId)
                .claim("userType", userType.toUpperCase())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Validar token
     */
    public boolean tokenValido(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        
        } catch (ExpiredJwtException e) {
            logger.warning("Token expirado: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.severe("Token não suportado: " + e.getMessage());
        } catch (MalformedJwtException e) {
            logger.warning("Token malformado: " + e.getMessage());
        } catch (io.jsonwebtoken.security.SignatureException e) {
            logger.severe("Assinatura inválida: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warning("Token vazio ou nulo: " + e.getMessage());
        } catch (Exception e) {
            logger.severe("Erro na validação do token: " + e.getMessage());
        }
        return false;
    }

    /**
     * Extrair username do token
     */
    public String getUsernameDoToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    /**
     * Extrair tipo de usuário do token
     */
    public String getUserTypeFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("userType", String.class);
    }

    /**
     * Extrair ID do usuário do token
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("userId", Long.class);
    }

    /**
     * Verificar se token expirou
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    // Extrair claims genéricos do token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    public long getExpirationTime(String token) {
        return extractClaim(token, Claims::getExpiration).getTime();
    }
}
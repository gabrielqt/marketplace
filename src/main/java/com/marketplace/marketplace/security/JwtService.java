package com.marketplace.marketplace.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    // chave secreta usada pra assinar o token — em produção vai no application.properties
    private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    // 24 horas em milissegundos
    private static final long EXPIRATION = 86400000;

    // gera o token com o email do usuário como subject
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())  // email do usuário
                .setIssuedAt(new Date())                // quando foi gerado
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION)) // quando expira
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // assina com a secret key
                .compact(); // converte pra String
    }

    // extrai o email do token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // valida se o token pertence ao usuário e não está expirado
    public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    // verifica se a data de expiração já passou
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

//    --> metodo genérico pra extrair qualquer informação do token
    // Claims é o "corpo" do token — contém subject, expiration, etc.
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token) // valida a assinatura e parseia
                .getBody();            // pega o corpo (Claims)
        return claimsResolver.apply(claims); // extrai o que foi pedido
    }

    // converte a SECRET_KEY string pra um objeto Key que o jjwt entende
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
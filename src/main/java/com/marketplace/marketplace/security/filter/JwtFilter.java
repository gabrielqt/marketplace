package com.marketplace.marketplace.security.filter;

import com.marketplace.marketplace.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    // OncePerRequestFilter garante que esse filtro executa UMA vez por requisição

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // pega o header "Authorization" da requisição
        // ele vem assim: "Bearer eyJhbGci..."
        String authHeader = request.getHeader("Authorization");

        // se não tem header ou não começa com "Bearer ", ignora e passa pro próximo filtro
        // isso cobre rotas públicas como /auth/login
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // remove o "Bearer " e fica só com o token
        String token = authHeader.substring(7);

        // extrai o email do token
        String username = jwtService.extractUsername(token);

        // se tem email e o usuário ainda não está autenticado nessa requisição
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // busca o usuário no banco pelo email
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // valida se o token é válido pra esse usuário
            if (jwtService.isTokenValid(token, userDetails)) {

                // cria o objeto de autenticação com o usuário e suas roles
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,                    // usuário
                                null,                           // credenciais (null pq já validamos)
                                userDetails.getAuthorities()    // roles
                        );

                // adiciona detalhes da requisição (IP, session, etc.)
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // registra o usuário como autenticado no contexto do Spring Security
                // a partir daqui o Spring sabe quem está fazendo a requisição
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // passa pra próxima etapa da cadeia de filtros
        filterChain.doFilter(request, response);
    }
}


//        ### O SecurityContextHolder — o que é?
//
//É onde o Spring guarda **quem está autenticado na requisição atual**. Pensa nele como uma variável global por thread:
//        ```
//Requisição 1 (usuário Arthur) → SecurityContext { authentication: Arthur }
//Requisição 2 (usuário Ana)    → SecurityContext { authentication: Ana }
package com.marketplace.marketplace.service;

import com.marketplace.marketplace.dto.request.LoginRequest;
import com.marketplace.marketplace.dto.request.RegisterRequest;
import com.marketplace.marketplace.dto.response.TokenResponse;
import com.marketplace.marketplace.entity.User;
import com.marketplace.marketplace.entity.enums.Role;
import com.marketplace.marketplace.exception.ObjectNotFoundException;
import com.marketplace.marketplace.repository.UserRepository;
import com.marketplace.marketplace.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;       // BCrypt que configuramos no SecurityConfig
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager; // gerencia o processo de autenticação

    public TokenResponse login(LoginRequest request) {

        // AuthenticationManager valida o email e senha automaticamente
        // se errado, lança exceção BadCredentialsException
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),    // username
                        request.password()  // senha em texto puro — Spring compara com o BCrypt do banco
                )
        );

        // se chegou aqui, credenciais estão corretas
        // busca o usuário no banco pelo email
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ObjectNotFoundException("User not found"));

        // gera o token JWT pro usuário
        String token = jwtService.generateToken(user);

        // retorna o token pro cliente
        return new TokenResponse(token);
    }

    public void register(RegisterRequest request) {

        // cria o usuário com a senha encriptada — NUNCA salva senha em texto puro
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password())) // BCrypt aqui
                .role(Role.BUYER)           // todo usuário novo começa como BUYER
                .balance(BigDecimal.ZERO)   // saldo começa zerado
                .build();

        userRepository.save(user);
    }
}
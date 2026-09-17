package com.grozniy.lab1.service;

import com.grozniy.lab1.config.JwtConfig;
import com.grozniy.lab1.dto.AuthResponse;
import com.grozniy.lab1.dto.LoginRequest;
import com.grozniy.lab1.dto.RegisterRequest;
import com.grozniy.lab1.dto.UserResponse;
import com.grozniy.lab1.exception.BadRequestException;
import com.grozniy.lab1.exception.ConflictException;
import com.grozniy.lab1.model.User;
import com.grozniy.lab1.repository.UserRepository;
import com.grozniy.lab1.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final int USERNAME_MIN = 3;
    private static final int USERNAME_MAX = 50;
    private static final int PASSWORD_MIN = 8;
    private static final int PASSWORD_MAX = 72; // BCrypt truncates beyond 72 bytes

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtConfig jwtConfig;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       JwtConfig jwtConfig) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.jwtConfig = jwtConfig;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        validateRegistration(request);

        String username = request.username().trim();
        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("Username already taken: " + username);
        }

        User user = User.create(username, passwordEncoder.encode(request.password()));
        User saved = userRepository.save(user);
        return new UserResponse(saved.getId(), saved.getUsername());
    }

    public AuthResponse login(LoginRequest request) {
        if (request.username() == null || request.username().isBlank()
                || request.password() == null || request.password().isBlank()) {
            throw new BadRequestException("Username and password are required");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username().trim(), request.password()));

        String token = jwtService.generateToken(authentication.getName());
        return new AuthResponse(token, "Bearer", jwtConfig.getExpirationMs());
    }

    private void validateRegistration(RegisterRequest request) {
        if (request.username() == null || request.username().isBlank()) {
            throw new BadRequestException("Username is required");
        }
        String username = request.username().trim();
        if (username.length() < USERNAME_MIN || username.length() > USERNAME_MAX) {
            throw new BadRequestException("Username must be between " + USERNAME_MIN + " and " + USERNAME_MAX + " characters");
        }
        if (!username.matches("[A-Za-z0-9_.-]+")) {
            throw new BadRequestException("Username may only contain letters, digits, '.', '_' and '-'");
        }
        if (request.password() == null || request.password().length() < PASSWORD_MIN
                || request.password().length() > PASSWORD_MAX) {
            throw new BadRequestException("Password must be between " + PASSWORD_MIN + " and " + PASSWORD_MAX + " characters");
        }
    }
}
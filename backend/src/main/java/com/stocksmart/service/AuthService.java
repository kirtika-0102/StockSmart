package com.stocksmart.service;

import java.util.List;
import java.util.Set;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stocksmart.dto.AuthResponse;
import com.stocksmart.dto.LoginRequest;
import com.stocksmart.dto.RegisterRequest;
import com.stocksmart.dto.UserResponse;
import com.stocksmart.entity.Role;
import com.stocksmart.entity.RoleName;
import com.stocksmart.entity.User;
import com.stocksmart.exception.BusinessException;
import com.stocksmart.repository.RoleRepository;
import com.stocksmart.repository.UserRepository;
import com.stocksmart.security.JwtService;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email is already registered");
        }

        Role staffRole = roleRepository.findByName(RoleName.STAFF)
                .orElseThrow(() -> new IllegalStateException("STAFF role is not seeded"));

        User user = new User();
        user.setUsername(request.username().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setActive(true);
        user.setRoles(Set.of(staffRole));
        userRepository.save(user);
        return toAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new BusinessException("Invalid credentials"));
        return toAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse me(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("User not found"));
        return toUserResponse(user);
    }

    private AuthResponse toAuthResponse(User user) {
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .sorted()
                .toList();
        String token = jwtService.generateToken(user.getUsername(), roles);
        return new AuthResponse(
                token,
                "Bearer",
                jwtService.getExpirationMs(),
                user.getUsername(),
                user.getFirstName() + " " + user.getLastName(),
                roles);
    }

    private UserResponse toUserResponse(User user) {
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .sorted()
                .toList();
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.isActive(),
                roles);
    }
}

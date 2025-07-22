package com.project.employee.service;

import com.project.employee.exception.ApiException;
import com.project.employee.model.User;
import com.project.employee.repository.UserRepository;
import com.project.employee.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public Map<String, String> login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException("User not found"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ApiException("Invalid credentials");
        }

        String accessToken = jwtUtil.generateAccessToken(username, user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(username, user.getRole());

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        return tokens;
    }

    public String register(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new ApiException("Username already exists");
        }
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role("USER") // or "ADMIN" if you want to register admins
                .build();
        userRepository.save(user);
        return "User registered successfully";
    }

    public Map<String, String> refresh(String refreshToken) {
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new ApiException("Invalid refresh token"));

        if (jwtUtil.validateJwtToken(refreshToken)) {
            throw new ApiException("Invalid refresh token");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getRole());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getRole());

        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", newRefreshToken);
        return tokens;
    }

    public void logout(String token) {
        User user = userRepository.findByRefreshToken(token)
                .orElseThrow(() -> new ApiException("Invalid refresh token"));

        user.setRefreshToken(null);
        userRepository.save(user);
    }
}
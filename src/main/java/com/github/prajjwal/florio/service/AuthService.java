package com.github.prajjwal.florio.service;

import com.github.prajjwal.florio.dto.AuthResponse;
import com.github.prajjwal.florio.repository.UserRepository;
import com.github.prajjwal.florio.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    public AuthResponse oauth2Login(String email, String name) {
        return null;
    }
}
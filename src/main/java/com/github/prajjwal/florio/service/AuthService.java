package com.github.prajjwal.florio.service;

import com.github.prajjwal.florio.dto.AuthResponse;
import com.github.prajjwal.florio.dto.RegistrationRequestDto;
import com.github.prajjwal.florio.model.user.User;
import com.github.prajjwal.florio.model.user.UserRole;
import com.github.prajjwal.florio.model.user.UserStatus;
import com.github.prajjwal.florio.repository.RefreshTokenRepository;
import com.github.prajjwal.florio.repository.UserRepository;
import com.github.prajjwal.florio.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Value("${app.refresh-token.expiration:604800000}")
    private Long refreshTokenExpiration;

    @Value("${app.login.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${app.login.lockout-duration-minutes:30}")
    private int lockoutDurationInMinutes;

    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final int MAX_OTP_ATTEMPTS = 5;

    @Transactional
    public void registerUser(RegistrationRequestDto register) {
        if (userRepository.existsByEmail(register.getEmail())) {
            log.warn("User already exist with email {}",  register.getEmail());
        }
        if (userRepository.existsByUsername(register.getUsername())) {
            log.warn("Username already taken");
        }

        User user = new User();
        user.setUsername(register.getUsername());
        user.setEmail(register.getEmail());
        user.setPassword(passwordEncoder.encode(register.getPassword()));
        user.setFirstName(register.getFirstName());
        user.setLastName(register.getLastName());
        user.setPhoneNumber(register.getPhoneNumber());
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(false);
        user.setFailedAttempts(0);

        userRepository.save(user);
        log.info("User {} registered successfully", register.getUsername());

        sendOtp(user.getEmail());
    }

    @Transactional
    public void verifyEmail(String email, String rawOtp) {

    }

    private void sendOtp(String email) {

    }



    public AuthResponse oauth2Login(String email, String name) {
        return null;
    }
}
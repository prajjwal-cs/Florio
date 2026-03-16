package com.github.prajjwal.florio.service;

import com.github.prajjwal.florio.dto.AuthResponse;
import com.github.prajjwal.florio.dto.LoginRequestDto;
import com.github.prajjwal.florio.dto.RegistrationRequestDto;
import com.github.prajjwal.florio.dto.TokenRefreshRequestDto;
import com.github.prajjwal.florio.model.RefreshToken;
import com.github.prajjwal.florio.model.user.User;
import com.github.prajjwal.florio.model.user.UserRole;
import com.github.prajjwal.florio.model.user.UserStatus;
import com.github.prajjwal.florio.repository.RefreshTokenRepository;
import com.github.prajjwal.florio.repository.UserRepository;
import com.github.prajjwal.florio.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;

    @Value("${app.refresh-token.expiration:604800000}")
    private Long refreshTokenExpiration;
    @Value("${app.login.max-failed-attempts:5}")
    private int maxFailedAttempts;
    @Value("${app.login.lockout-duration-minutes:30}")
    private int lockoutDurationInMinutes;

    @Transactional
    public void register(RegistrationRequestDto register) {
        if (userRepository.existsByEmail(register.getEmail())) {
            log.warn("User already exist with email {}", register.getEmail());
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

        otpService.generateAndSend(user.getEmail());
    }

    @Transactional
    public void verifyEmail(String email, String rawOtp) {
        otpService.verify(email, rawOtp);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        log.info("Email {} verified successfully", email);
    }

    public void resendOtp(String email) {
        if (!userRepository.existsByEmail(email)) {
            log.warn("OTP resend requested for unknown email {}", email);
            return;
        }
        otpService.generateAndSend(email);
        log.info("OTP resent for email {}", email);
    }

    // LOGIN ----
    @Transactional
    public AuthResponse login(LoginRequestDto loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        //pre-auth checks
        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new LockedException("Account is permanently blocked. Contact support.");
        }
        if (!user.isEmailVerified()) {
            throw new DisabledException("Email is not verified.");
        }
        if (isLockedOut(user)) {
            long minutesLeft = Duration.between(LocalDateTime.now(), user.getLockoutTime()).toMinutes() + 1;
            throw new LockedException("Account temporarily locked. Try again in " + minutesLeft + " minute(s).");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );
        } catch (BadCredentialsException ex) {
            handleFailedAttempts(user);
            if (isLockedOut(user)) {
                throw new LockedException("Account locked for " + lockoutDurationInMinutes +
                        "minutes due to too many failed attempts.");
            }
            int remaining = maxFailedAttempts - user.getFailedAttempts();
            throw new BadCredentialsException("Invalid username or password" + remaining + "attempts remaining.");
        }

        resetFailedAttempts(user);
        String accessToken = jwtTokenUtil.generateToken(user);
        String refreshToken = createRefreshToken(user);

        log.info("User logged in email={}", user.getEmail());

        return buildAuthResponse(accessToken, refreshToken, user);
    }

    @Transactional
    public void logout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        refreshTokenRepository.revokeAllByUser(user);
        log.info("User logged out email={}", email);
    }

    @Transactional
    public AuthResponse refreshToken(TokenRefreshRequestDto request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        User user = stored.getUser();
        if (stored.isRevoked()) {
            refreshTokenRepository.revokeAllByUser(stored.getUser());
            log.warn("Refresh token reused detected - all sessions revoked, email={}", user.getEmail());
            throw new IllegalArgumentException("Security violation detected, Please login again.");
        }
        if (stored.isExpired()) {
            stored.setRevoked(true);
            refreshTokenRepository.save(stored);
            throw new IllegalArgumentException("Refresh token expired, please login again");
        }

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        String newAccessToken = jwtTokenUtil.generateToken(user);
        String newRefreshToken = createRefreshToken(user);

        log.debug("Token rotated for email={}", user.getEmail());
        return buildAuthResponse(newAccessToken, newRefreshToken, user);
    }

    // OAuth2 login
    @Transactional
    public AuthResponse oauth2Login(String email, String name) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email not provided by OAuth2 provider." +
                    " Use email/password login instead");
        }

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createOAuth2User(email, name));

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new LockedException("Account is permanently blocked. Contact support.");
        }

        if (!user.isEmailVerified()) {
            user.setEmailVerified(true);
            userRepository.save(user);
        }
        resetFailedAttempts(user);

        String accessToken = jwtTokenUtil.generateToken(user);
        String refreshToken = createRefreshToken(user);

        log.info("OAuth2 login successful email={}", email);

        return buildAuthResponse(accessToken, refreshToken, user);
    }

    private User createOAuth2User(String email, String name) {
        String[] parts = (name != null && name.contains(" "))
                ? name.split(" ", 2)
                : new String[]{name != null ? name : email, ""};

        User user = new User();
        user.setEmail(email);
        user.setUsername(email);
        user.setFirstName(parts[0]);
        user.setLastName(parts[1]);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setPhoneNumber("");
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(UserRole.USER);
        user.setEmailVerified(true);
        user.setFailedAttempts(0);

        User saved = userRepository.save(user);
        log.info("OAuth2 user auto-created for email={}", email);
        return saved;
    }

    private String createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryTime(Instant.now().plusMillis(refreshTokenExpiration))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken).getToken();
    }

    private void handleFailedAttempts(User user) {
        int attempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(attempts);
        if (attempts >= maxFailedAttempts) {
            user.setLockoutTime(LocalDateTime.now().plusMinutes(lockoutDurationInMinutes));
            log.warn("Account locked after {} failed attempts, email={}", attempts, user.getEmail());
        }
        userRepository.save(user);
    }

    private boolean isLockedOut(User user) {
        if (user.getLockoutTime() == null) return false;
        if (LocalDateTime.now().isAfter(user.getLockoutTime())) {
            resetFailedAttempts(user);
            return false;
        }
        return true;
    }

    private void resetFailedAttempts(User user) {
        if (user.getFailedAttempts() != 0 || user.getLockoutTime() != null) {
            user.setFailedAttempts(0);
            user.setLockoutTime(null);
            userRepository.save(user);
        }
    }

    private AuthResponse buildAuthResponse(String accessToken, String refreshToken, User user) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
package com.github.prajjwal.florio.controller;

import com.github.prajjwal.florio.dto.AuthResponse;
import com.github.prajjwal.florio.dto.LoginRequestDto;
import com.github.prajjwal.florio.dto.RegistrationRequestDto;
import com.github.prajjwal.florio.dto.TokenRefreshRequestDto;
import com.github.prajjwal.florio.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegistrationRequestDto request) {
        authService.registerCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Registration Successful, verify your email."));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String email,
                                                           @RequestParam String otp) {
        authService.verifyEmail(email, otp);
        return ResponseEntity.ok(Map.of("message", "Email verified, You can now login."));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<Map<String, String>> resendOtp(@RequestParam String email) {
        authService.resendOtp(email);
        return ResponseEntity.ok(Map.of("message", "A new OTP has been sent."));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody TokenRefreshRequestDto request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@AuthenticationPrincipal UserDetails userDetails) {
        authService.logout(userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Logged out successfully!"));
    }

    // todo -> forgot password
}
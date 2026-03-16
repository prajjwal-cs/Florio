package com.github.prajjwal.florio.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    private static final String OTP_HASH_KEY = "otp:hash:";
    private static final String OTP_ATTEMPTS_KEY = "otp:attempts:";
    private static final String OTP_COOLDOWN_KEY = "otp:cooldown:";
    private static final Logger log = LogManager.getLogger(OtpService.class);
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final StringRedisTemplate redisTemplate;

    @Value("${app.otp.expiry-minutes}")
    private int otpExpiryInMinutes;

    @Value("${app.otp.max-attempts}")
    private int maxAttempts;

    @Value("${app.otp.cooldown-seconds}")
    private int cooldownSeconds;

    public OtpService(PasswordEncoder passwordEncoder, EmailService emailService, StringRedisTemplate redisTemplate) {
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.redisTemplate = redisTemplate;
    }

    public void generateAndSend(String email) {
        if (isOnCooldown(email)) {
            throw new IllegalStateException("Please wait " + cooldownSeconds +
                    "seconds before sending a new OTP");
        }

        String rawOtp = generateRawOtp();

        redisTemplate.opsForValue().set(OTP_HASH_KEY + email, passwordEncoder.encode(rawOtp),
                otpExpiryInMinutes, TimeUnit.MINUTES
        );

        redisTemplate.delete(OTP_ATTEMPTS_KEY + email);

        redisTemplate.opsForValue().set(
                OTP_COOLDOWN_KEY + email,
                "1",
                cooldownSeconds, TimeUnit.SECONDS
        );

        emailService.sendOtpEmail(email, rawOtp);
        log.debug("OTP generated and sent to email= {}", email);
    }

    public void verify(String email, String rawOtp) {
        log.debug("Verifying OTP for email='{}', otp='{}'", email, rawOtp);
        log.debug("Redis key lookup: '{}'", OTP_HASH_KEY + email);
        log.debug("Stored hash exists: {}", redisTemplate.hasKey(OTP_HASH_KEY + email));
        String attemptStr = redisTemplate.opsForValue().get(OTP_ATTEMPTS_KEY + email);
        int attempts = attemptStr != null ? Integer.parseInt(attemptStr) : 0;
        if (attempts >= maxAttempts) {
            throw new IllegalStateException(
                    "Too many failed OTP attempts! Please try again later for new OTP.");
        }

        String storedHash = redisTemplate.opsForValue().get(OTP_HASH_KEY + email);

        if (storedHash == null) {
            throw new IllegalArgumentException("OTP has expired or never issued, Please request a new one.");
        }

        if (!passwordEncoder.matches(rawOtp, storedHash)) {
            incrementAttempts(email, attempts);
            int remaining = maxAttempts - (attempts + 1);
            throw new IllegalArgumentException("Invalid OTP!" + remaining + "attempt(s) remaining");
        }

        redisTemplate.delete(OTP_HASH_KEY + email);
        redisTemplate.delete(OTP_ATTEMPTS_KEY + email);
        redisTemplate.delete(OTP_COOLDOWN_KEY + email);

        log.debug("OTP verified successfully, for email= {}", email);
    }

    private void incrementAttempts(String email, int attempts) {
        String key = OTP_ATTEMPTS_KEY + email;
        redisTemplate.opsForValue().set(
                key,
                String.valueOf(attempts + 1),
                otpExpiryInMinutes, TimeUnit.MINUTES
        );
    }

    private String generateRawOtp() {
        return String.valueOf(100_000 + new Random().nextInt(900_000));
    }

    private boolean isOnCooldown(String email) {
        return redisTemplate.hasKey(OTP_COOLDOWN_KEY + email);
    }

}
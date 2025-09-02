package com.github.prajjwal.florio.repository;

import com.github.prajjwal.florio.model.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpRepository extends JpaRepository<Otp, UUID> {
    Optional<Otp> findByEmailAndOtpCodeAndUsedFalse(String email, String otpCode);
    void deleteByEmail(String email);
}

package com.github.prajjwal.florio.dto;

import com.github.prajjwal.florio.model.user.UserRole;
import com.github.prajjwal.florio.model.user.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserProfileResponseDto {
    private UUID userId;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private UserRole role;
    private UserStatus status;
    private boolean isEmailVerified;
    private Instant createdAt;

    private String specialization;
    private Double rating;
    private boolean isAvailable;
    private String experience;
    private Integer totalJobs;
}
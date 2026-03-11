package com.github.prajjwal.florio.dto;

import com.github.prajjwal.florio.model.booking.ServiceStatus;
import com.github.prajjwal.florio.model.booking.ServiceType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ServiceBookingResponseDto {
    private UUID id;
    private String customerEmail;
    private String servicePartnerEmail;
    private ServiceType serviceType;
    private String description;
    private String address;
    private Instant preferredDateTime;
    private ServiceStatus status;
    private Double estimatedPrice;
    private Double finalPrice;
    private Instant createdAt;
    private Instant completedAt;
    private Double rating;
    private String feedback;

}
package com.github.prajjwal.florio.dto;

import com.github.prajjwal.florio.model.booking.ServiceType;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
public class ServiceBookingRequestDto {
    private ServiceType serviceType;
    private String description;
    private String address;
    private Instant preferredDateTime;
}
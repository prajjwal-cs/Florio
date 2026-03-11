/* Created by IntelliJ IDEA.

Author: Prajjwal Pachauri
Date: 29-08-2025
Time: 4:46 pm
File: ServiceRequest.java */
package com.github.prajjwal.florio.model.booking;

import com.github.prajjwal.florio.model.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "service_request")
@Getter
@Setter
public class ServiceBooking {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_partner_id")
    private User servicePartner;

    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;

    private String description;

    private String address;

    @Column(name = "preferred_date")
    private Instant preferredDate;

    @Enumerated(EnumType.STRING)
    private ServiceStatus status = ServiceStatus.PENDING;

    @Column(name = "estimated_price")
    private Double estimatedPrice;

    @Column(name = "final_price")
    private Double finalPrice;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    private Double rating;
    private String feedback;

    @PrePersist
    public void onCreate() {
        createdAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceBooking that = (ServiceBooking) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
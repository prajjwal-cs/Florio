/* Created by IntelliJ IDEA.

Author: Prajjwal Pachauri
Date: 29-08-2025
Time: 4:46 pm
File: ServiceRequest.java */
package com.github.prajjwal.florio.model.booking;

import com.github.prajjwal.florio.model.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "service_request")
@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
public class ServiceBooking {
    @Id
    @UuidGenerator
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

    @Column(name = "preffered_date")
    private LocalDateTime prefferedDate;

    @Enumerated(EnumType.STRING)
    private ServiceStatus status = ServiceStatus.PENDING;

    @Column(name = "estimated_price")
    private Double estimatedPrice;

    @Column(name = "final_price")
    private Double finalPrice;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    private Integer rating;
    private String feedback;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ServiceBooking that = (ServiceBooking) o;
        return Objects.equals(id, that.id) && Objects.equals(customer, that.customer) && Objects.equals(servicePartner, that.servicePartner) && serviceType == that.serviceType && Objects.equals(description, that.description) && Objects.equals(address, that.address) && Objects.equals(prefferedDate, that.prefferedDate) && status == that.status && Objects.equals(estimatedPrice, that.estimatedPrice) && Objects.equals(finalPrice, that.finalPrice) && Objects.equals(createdAt, that.createdAt) && Objects.equals(completedAt, that.completedAt) && Objects.equals(rating, that.rating) && Objects.equals(feedback, that.feedback);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, customer, servicePartner, serviceType, description, address, prefferedDate, status, estimatedPrice, finalPrice, createdAt, completedAt, rating, feedback);
    }

    @Override
    public String toString() {
        return "ServiceRequest{" +
                "id=" + id +
                ", customer=" + customer +
                ", servicePartner=" + servicePartner +
                ", serviceType=" + serviceType +
                ", description='" + description + '\'' +
                ", address='" + address + '\'' +
                ", prefferedDate=" + prefferedDate +
                ", status=" + status +
                ", estimatedPrice=" + estimatedPrice +
                ", finalPrice=" + finalPrice +
                ", createdAt=" + createdAt +
                ", completedAt=" + completedAt +
                ", rating=" + rating +
                ", feedback='" + feedback + '\'' +
                '}';
    }
}
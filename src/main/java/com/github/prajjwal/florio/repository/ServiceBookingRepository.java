/* Created by IntelliJ IDEA.

Author: Prajjwal Pachauri
Date: 02-09-2025
Time: 1:39 pm
File: bookingRepository.java */
package com.github.prajjwal.florio.repository;

import com.github.prajjwal.florio.model.booking.ServiceBooking;
import com.github.prajjwal.florio.model.booking.ServiceStatus;
import com.github.prajjwal.florio.model.booking.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceBookingRepository extends JpaRepository<ServiceBooking, UUID> {
    List<ServiceBooking> findByCustomerId(UUID customerId);
    List<ServiceBooking> findByServicePartnerId(UUID servicePartnerId);
    List<ServiceBooking> findByStatus(ServiceStatus status);
    List<ServiceBooking> findByServiceTypeAndStatus(ServiceType serviceType, ServiceStatus status);
}
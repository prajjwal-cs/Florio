/* Created by IntelliJ IDEA.

Author: Prajjwal Pachauri
Date: 02-09-2025
Time: 1:39 pm
File: bookingRepository.java */
package com.github.prajjwal.florio.repository;

import com.github.prajjwal.florio.model.booking.ServiceBooking;
import com.github.prajjwal.florio.model.booking.ServiceStatus;
import com.github.prajjwal.florio.model.booking.ServiceType;
import com.github.prajjwal.florio.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceBookingRepository extends JpaRepository<ServiceBooking, UUID> {

    Page<ServiceBooking> findByCustomer(User customer, Pageable pageable);

    Page<ServiceBooking> findByCustomerAndStatus(User customer, ServiceStatus status,  Pageable pageable);

    Page<ServiceBooking> findByServicePartner(User servicePartner, Pageable pageable);

    Page<ServiceBooking> findByServicePartnerAndStatus(User servicePartner, ServiceStatus status,  Pageable pageable);

    Page<ServiceBooking> findByStatus(ServiceStatus status,  Pageable pageable);

    Page<ServiceBooking> findByServiceType(ServiceType serviceType, Pageable pageable);

    Page<ServiceBooking> findByServicePartnerIsNullAndStatus(ServiceStatus status, Pageable pageable);
}
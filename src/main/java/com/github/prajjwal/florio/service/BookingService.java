package com.github.prajjwal.florio.service;

import com.github.prajjwal.florio.dto.ServiceBookingRequestDto;
import com.github.prajjwal.florio.dto.ServiceBookingResponseDto;
import com.github.prajjwal.florio.model.booking.ServiceBooking;
import com.github.prajjwal.florio.model.booking.ServiceStatus;
import com.github.prajjwal.florio.model.user.User;
import com.github.prajjwal.florio.model.user.UserRole;
import com.github.prajjwal.florio.repository.ServiceBookingRepository;
import com.github.prajjwal.florio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final Logger log = LogManager.getLogger(BookingService.class);
    private final ServiceBookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Transactional
    public ServiceBookingResponseDto createBooking(String customerEmail, ServiceBookingRequestDto request) {
        User customer = findUserByEmailOrThrow(customerEmail);

        ServiceBooking booking = new ServiceBooking();
        booking.setCustomer(customer);
        booking.setServiceType(request.getServiceType());
        booking.setDescription(request.getDescription());
        booking.setAddress(request.getAddress());
        booking.setPreferredDate(request.getPreferredDateTime());
        booking.setStatus(ServiceStatus.PENDING);

        ServiceBooking saved = bookingRepository.save(booking);
        log.info("Booking created bookingId={} by email={}", saved.getId(), customerEmail);
        return changeToResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<ServiceBookingResponseDto> getMyBookings(String customerEmail, Pageable pageable) {
        User customer = findUserByEmailOrThrow(customerEmail);
        return bookingRepository.findByCustomer(customer, pageable).map(this::changeToResponse);
    }

    @SneakyThrows
    @Transactional(readOnly = true)
    public ServiceBookingResponseDto getBookingById(String requestEmail, UUID id) {
        ServiceBooking booking = findBookingOrThrow(id);
        User requester = findUserByEmailOrThrow(requestEmail);

        boolean isOwner = booking.getCustomer().getEmail().equals(requestEmail);
        boolean isPartner = booking.getServicePartner() != null &&
                booking.getServicePartner().getEmail().equals(requestEmail);
        boolean isAdmin = requester.getRole() == UserRole.ADMIN;

        if (!isOwner && !isPartner && !isAdmin) {
            throw new AccessDeniedException("You are not allowed to perform this action");
        }

        return changeToResponse(booking);
    }

    @SneakyThrows
    @Transactional
    public ServiceBookingResponseDto cancelBooking(String customerEmail, UUID id) {
        ServiceBooking booking = findBookingOrThrow(id);

        if (!booking.getCustomer().getEmail().equals(customerEmail)) {
            throw new AccessDeniedException("You can only cancel your own bookings");
        }
        if (booking.getStatus() == ServiceStatus.IN_PROGRESS ||
                booking.getStatus() == ServiceStatus.COMPLETED) {
            throw new IllegalStateException("Can not cancel booking that is already " +
                    booking.getStatus().name().toLowerCase());
        }
        booking.setStatus(ServiceStatus.CANCELED);
        bookingRepository.save(booking);
        log.info("Booking cancelled bookingId={} by email={}", booking.getId(), customerEmail);
        return changeToResponse(booking);
    }

    @SneakyThrows
    @Transactional
    public ServiceBookingResponseDto submitFeedback(String customerEmail, UUID bookingId,
                                                    Integer rating, String feedback) {
        ServiceBooking booking = findBookingOrThrow(bookingId);
        if (!booking.getCustomer().getEmail().equals(customerEmail)) {
            throw new AccessDeniedException("You can only rate your own bookings");
        }
        if (booking.getStatus() != ServiceStatus.COMPLETED) {
            throw new IllegalStateException("Can only rate completed bookings");
        }
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        booking.setRating(rating);
        booking.setFeedback(feedback);
        bookingRepository.save(booking);

        if (booking.getServicePartner() != null) {
            updatePartnerRating(booking.getServicePartner());
        }
        log.info("Feedback submitted for bookingId={} by user={}",
                bookingId, booking.getCustomer().getUsername());

        return changeToResponse(booking);
    }

    @Transactional(readOnly = true)
    public Page<ServiceBookingResponseDto> getAvailableJobs(Pageable pageable) {
        return bookingRepository
                .findByServicePartnerIsNullAndStatus(ServiceStatus.PENDING, pageable)
                .map(this::changeToResponse);
    }

    @Transactional
    public ServiceBookingResponseDto acceptJob(String partnerEmail, UUID bookingId) {
        ServiceBooking booking = findBookingOrThrow(bookingId);
        User partner = findUserByEmailOrThrow(partnerEmail);

        if (booking.getStatus() != ServiceStatus.PENDING) {
            throw new IllegalStateException("This job is no longer available");
        }
        if (booking.getServicePartner() != null) {
            throw new IllegalStateException("This job has already been accepted");
        }

        booking.setServicePartner(partner);
        booking.setStatus(ServiceStatus.ACCEPTED);
        bookingRepository.save(booking);
        log.info("Job accepted for bookingId={} by partner={}", bookingId, partner.getUsername());
        return changeToResponse(booking);
    }

    @SneakyThrows
    @Transactional
    public ServiceBookingResponseDto updateJobStatus(String partnerEmail, UUID bookingId,
                                                     ServiceStatus newStatus) {
        ServiceBooking booking = findBookingOrThrow(bookingId);

        if (booking.getServicePartner() == null ||
                !booking.getServicePartner().getEmail().equals(partnerEmail)) {
            throw new AccessDeniedException("You are not assigned to this job");
        }

        validateStatusTransition(booking.getStatus(), newStatus);

        booking.setStatus(newStatus);
        if (newStatus == ServiceStatus.COMPLETED) {
            booking.setCompletedAt(Instant.now());
            incrementPartnerJobCount(booking.getServicePartner());
        }
        bookingRepository.save(booking);
        log.info("Job status updated to {}, for bookingId={}, by partner={}",
                newStatus, bookingId, booking.getServicePartner().getUsername());
        return changeToResponse(booking);
    }

    @Transactional(readOnly = true)
    public Page<ServiceBookingResponseDto> getMyJobs(String partnerEmail, Pageable pageable) {
        User partner = findUserByEmailOrThrow(partnerEmail);
        return bookingRepository.findByServicePartner(partner, pageable)
                .map(this::changeToResponse);
    }

    @Transactional(readOnly = true)
    public Page<ServiceBookingResponseDto> getAllBookings(Pageable pageable) {
        return bookingRepository.findAll(pageable).map(this::changeToResponse);
    }

    @Transactional
    public ServiceBookingResponseDto setEstimatedPrice(UUID bookingId, Double price) {
        ServiceBooking booking = findBookingOrThrow(bookingId);
        booking.setEstimatedPrice(price);
        bookingRepository.save(booking);
        return changeToResponse(booking);
    }

    public ServiceBookingResponseDto setFinalPrice(UUID bookingId, Double price) {
        ServiceBooking booking = findBookingOrThrow(bookingId);
        booking.setFinalPrice(price);
        bookingRepository.save(booking);
        return changeToResponse(booking);
    }

    private void incrementPartnerJobCount(User servicePartner) {
        servicePartner.setTotalJobs(servicePartner.getTotalJobs() + 1);
        userRepository.save(servicePartner);
    }

    private void validateStatusTransition(ServiceStatus current, ServiceStatus next) {
        boolean valid =
                switch (current) {
                    case ACCEPTED -> next == ServiceStatus.IN_PROGRESS;
                    case IN_PROGRESS -> next == ServiceStatus.COMPLETED;
                    default -> false;
                };
        if (!valid) {
            throw new IllegalStateException("Can not transition from " + current + " to " + next);
        }
    }

    private void updatePartnerRating(User partner) {
        Page<ServiceBooking> jobs = bookingRepository.findByServicePartnerAndStatus(
                partner, ServiceStatus.COMPLETED, Pageable.unpaged());

        double avg = jobs.getContent().stream()
                .filter(booking -> booking.getRating() != null)
                .mapToDouble(ServiceBooking::getRating)
                .average()
                .orElse(0.0);

        partner.setRating(avg);
        userRepository.save(partner);
    }


    private ServiceBooking findBookingOrThrow(UUID id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
    }

    private User findUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }

    private ServiceBookingResponseDto changeToResponse(ServiceBooking booking) {
        return ServiceBookingResponseDto.builder()
                .id(booking.getId())
                .customerEmail(booking.getCustomer().getEmail())
                .servicePartnerEmail(booking.getServicePartner() != null
                        ? booking.getServicePartner().getEmail() : null)
                .serviceType(booking.getServiceType())
                .description(booking.getDescription())
                .address(booking.getAddress())
                .preferredDateTime(booking.getPreferredDate())
                .status(booking.getStatus())
                .estimatedPrice(booking.getEstimatedPrice())
                .finalPrice(booking.getFinalPrice())
                .createdAt(booking.getCreatedAt())
                .completedAt(booking.getCompletedAt())
                .rating(booking.getRating())
                .feedback(booking.getFeedback())
                .build();
    }
}
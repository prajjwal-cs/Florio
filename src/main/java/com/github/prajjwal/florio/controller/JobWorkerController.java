package com.github.prajjwal.florio.controller;

import com.github.prajjwal.florio.dto.AvailabilityRequest;
import com.github.prajjwal.florio.dto.ServiceBookingResponseDto;
import com.github.prajjwal.florio.dto.UpdateProfileRequestDto;
import com.github.prajjwal.florio.dto.UserProfileResponseDto;
import com.github.prajjwal.florio.model.booking.ServiceStatus;
import com.github.prajjwal.florio.service.BookingService;
import com.github.prajjwal.florio.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/job-worker")
@RequiredArgsConstructor
public class JobWorkerController {

    private final BookingService bookingService;
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponseDto> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getProfile(userDetails.getUsername()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponseDto> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequestDto request) {
        return ResponseEntity.ok(
                userService.updateProfile(userDetails.getUsername(), request)
        );
    }

    @GetMapping("/jobs/available")
    public ResponseEntity<Page<ServiceBookingResponseDto>> getAvailableJobs(
            @PageableDefault(size = 20)Pageable pageable) {
        return ResponseEntity.ok(bookingService.getAvailableJobs(pageable));
    }

    @PutMapping("/jobs/accept/{id}")
    public ResponseEntity<ServiceBookingResponseDto> acceptJob(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.acceptJob(userDetails.getUsername(), id));
    }

    @PutMapping("/jobs/status/{id}")
    public ResponseEntity<ServiceBookingResponseDto> updateJobStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @RequestParam String status) {
        return ResponseEntity.ok(
                bookingService.updateJobStatus(userDetails.getUsername(), id, status)
        );
    }

    @GetMapping("/jobs")
    public ResponseEntity<Page<ServiceBookingResponseDto>> getMyJobs(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(bookingService.getMyJobs(userDetails.getUsername(), pageable));
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<ServiceBookingResponseDto> getJobById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.getBookingById(userDetails.getUsername(), id));
    }

    @GetMapping("/availability")
    public ResponseEntity<Boolean> getAvailability(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getAvailability(userDetails.getUsername()));
    }

//    availability
    @PutMapping("/availability")
    public ResponseEntity<Boolean> toggleAvailability(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody AvailabilityRequest request) {
        userService.toggleAvailability(userDetails.getUsername(), request.getIsAvailable());
        return ResponseEntity.ok(request.getIsAvailable());
    }
}
package com.github.prajjwal.florio.controller;

import com.github.prajjwal.florio.dto.*;
import com.github.prajjwal.florio.service.BookingService;
import com.github.prajjwal.florio.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final BookingService bookingService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponseDto> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getProfile(userDetails.getUsername()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponseDto> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequestDto updateRequest) {
        return ResponseEntity.ok(userService.updateProfile(userDetails.getUsername(), updateRequest));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Map<String, String>> updatePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequestDto request) {
        userService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(Map.of("message", "password changed successfully"));
    }

    @PostMapping("/bookings")
    public ResponseEntity<ServiceBookingResponseDto> createBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ServiceBookingRequestDto request) {
        return ResponseEntity.ok(
                bookingService.createBooking(userDetails.getUsername(), request)
        );
    }

    @GetMapping("/bookings")
    public ResponseEntity<Page<ServiceBookingResponseDto>> getMyBookings(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        return ResponseEntity.ok(
                bookingService.getMyBookings(userDetails.getUsername(), pageable)
        );
    }

    @GetMapping("/bookings/{id}")
    public ResponseEntity<ServiceBookingResponseDto> getBookingById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        return ResponseEntity.ok(
                bookingService.getBookingById(userDetails.getUsername(), id)
        );
    }

    @PutMapping("/bookings/{id}/cancel")
    public ResponseEntity<ServiceBookingResponseDto> cancelBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        return ResponseEntity.ok(
                bookingService.cancelBooking(userDetails.getUsername(), id)
        );
    }

    @PostMapping("/bookings/{id}/feedback")
    public ResponseEntity<ServiceBookingResponseDto> submitFeedback(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @RequestParam Integer rating,
            @RequestParam(required = false) String feedback) {
        return ResponseEntity.ok(
                bookingService.submitFeedback(userDetails.getUsername(), id, rating, feedback)
        );
    }
}
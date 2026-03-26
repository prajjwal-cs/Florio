package com.github.prajjwal.florio.controller;

import com.github.prajjwal.florio.dto.RegistrationRequestDto;
import com.github.prajjwal.florio.dto.ServiceBookingResponseDto;
import com.github.prajjwal.florio.dto.UserProfileResponseDto;
import com.github.prajjwal.florio.service.AuthService;
import com.github.prajjwal.florio.service.BookingService;
import com.github.prajjwal.florio.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;
    private final BookingService bookingService;
    private final AuthService authService;

    @PostMapping("/workers/register")
    public ResponseEntity<Map<String, String>> registerWorker(
            @Valid @RequestBody RegistrationRequestDto request) {
        request.setRole("WORKER");
        authService.registerWorker(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Service Partner registered successfully."));
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserProfileResponseDto>> getALlUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserProfileResponseDto> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping("/users/{id}/block")
    public ResponseEntity<Map<String, String>> blockUser(@PathVariable UUID id) {
        userService.blockUser(id);
        return ResponseEntity.ok(Map.of("message", "User blocked successfully"));
    }

    @PostMapping("/users/{id}/unblock")
    public ResponseEntity<Map<String, String>> unblockUser(@PathVariable UUID id) {
        userService.unblockUser(id);
        return ResponseEntity.ok(Map.of("message", "User unblocked successfully"));
    }

    @GetMapping("/bookings")
    public ResponseEntity<Page<ServiceBookingResponseDto>> getAllBookings(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(bookingService.getAllBookings(pageable));
    }

    @GetMapping("/bookings/{id}")
    public ResponseEntity<ServiceBookingResponseDto> getBookingById(@PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.getBookingById("admin", id));
    }

    @PutMapping("/bookings/{id}/estimated-price")
    public ResponseEntity<ServiceBookingResponseDto> setEstimatedPrice(
            @PathVariable UUID id, @RequestParam Double price) {
        return ResponseEntity.ok(bookingService.setEstimatedPrice(id, price));
    }

    @PutMapping("/bookings/{id}/final-price")
    public ResponseEntity<ServiceBookingResponseDto> setFinalPrice(
            @PathVariable UUID id, @RequestParam Double price) {
        return ResponseEntity.ok(bookingService.setFinalPrice(id, price));
    }

}
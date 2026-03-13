package com.github.prajjwal.florio.service;

import com.github.prajjwal.florio.dto.ChangePasswordRequestDto;
import com.github.prajjwal.florio.dto.UpdateProfileRequestDto;
import com.github.prajjwal.florio.dto.UserProfileResponseDto;
import com.github.prajjwal.florio.model.user.User;
import com.github.prajjwal.florio.model.user.UserStatus;
import com.github.prajjwal.florio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserProfileResponseDto getProfile(String email) {
        User user = findByEmailOrThrow(email);
        return changeToResponse(user);
    }

    @Transactional
    public UserProfileResponseDto updateProfile(String email, UpdateProfileRequestDto request) {
        User user = findByEmailOrThrow(email);

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            user.setCity(request.getCity());
        }
        if (request.getState() != null) {
            user.setState(request.getState());
        }
        if (request.getZipCode() != null) {
            user.setZipCode(request.getZipCode());
        }
        if (request.getSpecialization() != null) {
            user.setSpecialization(request.getSpecialization());
        }
        if (request.getExperience() != null) {
            user.setExperience(request.getExperience());
        }
        if (request.getIsAvailable() != null) {
            user.setAvailable(request.getIsAvailable());
        }

        userRepository.save(user);
        log.info("Profile updated for email={}", email);
        return changeToResponse(user);
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequestDto request) {
        User user = findByEmailOrThrow(email);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for email={}", email);
    }

    @Transactional(readOnly = true)
    public Page<UserProfileResponseDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::changeToResponse);
    }

    @Transactional(readOnly = true)
    public UserProfileResponseDto getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return changeToResponse(user);
    }

    @Transactional
    public void blockUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setStatus(UserStatus.BLOCKED);
        userRepository.save(user);
        log.info("User blocked with username={}", user.getUsername());
    }

    @Transactional
    public void unblockUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setStatus(UserStatus.ACTIVE);
        user.setFailedAttempts(0);
        user.setLockoutTime(null);
        userRepository.save(user);
        log.info("User unblocked with username={}", user.getUsername());
    }



    public UserProfileResponseDto changeToResponse(User user) {
        return UserProfileResponseDto.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .city(user.getCity())
                .state(user.getState())
                .zipCode(user.getZipCode())
                .role(user.getRole())
                .status(user.getStatus())
                .isEmailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .specialization(user.getSpecialization())
                .rating(user.getRating())
                .isAvailable(user.isAvailable())
                .experience(user.getExperience())
                .totalJobs(user.getTotalJobs())
                .build();
    }

    private User findByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}
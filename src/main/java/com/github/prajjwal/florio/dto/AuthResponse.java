package com.github.prajjwal.florio.dto;

import com.github.prajjwal.florio.model.user.UserRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private UserRole role;
    private String email;
}
/* Created by IntelliJ IDEA.

Author: Prajjwal Pachauri
Date: 01-09-2025
Time: 3:34 pm
File: Otp.java */
package com.github.prajjwal.florio.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "otp")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Otp {
    @Id
    @UuidGenerator
    private UUID otpId;

    private String email;
    private String  otpCode;
    private LocalDateTime expiryTime;
    private LocalDateTime createdAt =  LocalDateTime.now();
    private boolean used =  false;

}
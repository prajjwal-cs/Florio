package com.github.prajjwal.florio.dto;

import lombok.Data;

@Data
public class UpdateProfileRequestDto {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private String city;
    private String state;
    private String zipCode;

    private String specialization;
    private String experience;
    private Boolean isAvailable;
}
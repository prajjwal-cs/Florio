/* Created by IntelliJ IDEA.

Author: Prajjwal Pachauri
Date: 28-08-2025
Time: 12:30 pm
File: User.java */
package com.github.prajjwal.florio.model.user;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private String role;

    @Column(columnDefinition = "TEXT")
    private String address;
    private  String city;
    private String state;
    private String zipCode;
    private String country;

    @Column(nullable = false)
    private boolean isActive =  true;

    @Column(nullable = false)
    private boolean emailVerified = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
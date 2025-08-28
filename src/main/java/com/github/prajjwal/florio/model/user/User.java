/* Created by IntelliJ IDEA.

Author: Prajjwal Pachauri
Date: 28-08-2025
Time: 12:30 pm
File: User.java */
package com.github.prajjwal.florio.model.user;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "user")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String username;

    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(columnDefinition = "TEXT")
    private String address;
    private  String city;
    private String state;
    private String zipCode;

    // for service makers
    private String specialization;
    private Double rating;
    private Integer totalRating;
    private boolean isAvailable;
    private String documentVerification;
    private String profilePicture;

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return isAvailable == user.isAvailable && isActive == user.isActive && emailVerified == user.emailVerified && Objects.equals(id, user.id) && Objects.equals(email, user.email) && Objects.equals(username, user.username) && Objects.equals(password, user.password) && Objects.equals(firstName, user.firstName) && Objects.equals(lastName, user.lastName) && Objects.equals(phoneNumber, user.phoneNumber) && role == user.role && Objects.equals(address, user.address) && Objects.equals(city, user.city) && Objects.equals(state, user.state) && Objects.equals(zipCode, user.zipCode) && Objects.equals(specialization, user.specialization) && Objects.equals(rating, user.rating) && Objects.equals(totalRating, user.totalRating) && Objects.equals(documentVerification, user.documentVerification) && Objects.equals(profilePicture, user.profilePicture) && Objects.equals(createdAt, user.createdAt) && Objects.equals(updatedAt, user.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, username, password, firstName, lastName, phoneNumber, role, address, city, state, zipCode, specialization, rating, totalRating, isAvailable, documentVerification, profilePicture, isActive, emailVerified, createdAt, updatedAt);
    }
}
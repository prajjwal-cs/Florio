package com.github.prajjwal.florio.model;

import com.github.prajjwal.florio.model.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_token", indexes = {
        @Index(name = "idx_refresh_token", columnList = "token", unique = true),
        @Index(name = "idx_refresh_token_user", columnList = "user_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 512)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Instant expiryTime;

    @Column(nullable = false)
    private boolean revoked;

    @Column(updatable = false)
    private Instant createdAt;


    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiryTime);
    }
}
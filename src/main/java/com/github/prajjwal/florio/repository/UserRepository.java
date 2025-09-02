package com.github.prajjwal.florio.repository;

import com.github.prajjwal.florio.model.user.User;
import com.github.prajjwal.florio.model.user.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(UserRole role);

    @Query("SELECT u FROM User u WHERE u.role = 'SERVICE_PARTNER' AND UPPER(u.specialization) = UPPER(:specialization)")
    Optional<User> findServicePartnerBySpecialization(@Param(value = "specialization") String specialization);
}
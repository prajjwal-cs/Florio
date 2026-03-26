package com.github.prajjwal.florio.repository;

import com.github.prajjwal.florio.model.booking.ServiceType;
import com.github.prajjwal.florio.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.role = 'JOB_WORKER' AND UPPER(u.specialization) = UPPER(:specialization)")
    Optional<User> findJobWorkerBySpecialization(@Param(value = "specialization") String specialization);


    @Query("""
        SELECT u FROM User u
        WHERE u.role = 'WORKER' AND u.isAvailable = true
        AND LOWER(u.city) = LOWER(:city)
        AND u.specialization = :specialization
        ORDER BY u.rating DESC
    """)
    List<User> findAvailablePartnersByCityAndSpecialization(
            @Param("city") String city, @Param("s") String specialization);
}
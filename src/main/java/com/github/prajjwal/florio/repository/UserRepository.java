package com.github.prajjwal.florio.repository;

import com.github.prajjwal.florio.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.role = 'JOB_WORKER' AND UPPER(u.specialization) = UPPER(:specialization)")
    Optional<User> findJobWorkerBySpecialization(@Param(value = "specialization") String specialization);
}
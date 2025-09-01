package com.github.prajjwal.florio.repository;

import com.github.prajjwal.florio.model.user.User;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface UserRepository extends Repository<User, UUID> {
}
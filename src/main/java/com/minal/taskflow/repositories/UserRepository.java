package com.minal.taskflow.repositories;

import com.minal.taskflow.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserModel, UUID> {
    Optional<UserModel> findByUserName(String userName);
    Optional<UserModel> findByEmail(String email);
}

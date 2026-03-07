package com.minal.taskflow.repositories;

import com.minal.taskflow.models.TaskModel;
import com.minal.taskflow.models.UserModel;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends CrudRepository<TaskModel, UUID> {
    Optional<TaskModel> findByIdAndUser(UUID id, UserModel user);
    List<TaskModel> findByUser(UserModel user);
}

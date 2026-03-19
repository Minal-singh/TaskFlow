package com.minal.taskflow.repositories;

import com.minal.taskflow.models.TaskModel;
import com.minal.taskflow.models.UserModel;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends CrudRepository<TaskModel, UUID> {
    List<TaskModel> findByAssignee(UserModel user);
    List<TaskModel> findByReporter(UserModel user);

    @Query("SELECT w.user.userName FROM TaskWatcher w WHERE w.id.taskId = :taskId")
    List<String> findWatcherUserNamesByTaskId(@Param("taskId") UUID taskId);
}

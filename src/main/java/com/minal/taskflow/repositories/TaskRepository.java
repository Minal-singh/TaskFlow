package com.minal.taskflow.repositories;

import com.minal.taskflow.models.TaskModel;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends CrudRepository<TaskModel, UUID> {
    @Query("SELECT t FROM TaskModel t WHERE t.assignee.userName = :userName")
    List<TaskModel> findByAssigneeUserName(@Param("userName") String userName);

    @Query("SELECT t FROM TaskModel t WHERE t.reporter.userName = :userName")
    List<TaskModel> findByReporterUserName(@Param("userName") String userName);

    @Query("SELECT w.user.userName FROM TaskWatcher w WHERE w.id.taskId = :taskId")
    List<String> findWatcherUserNamesByTaskId(@Param("taskId") UUID taskId);
}


package com.minal.taskflow.repositories;

import com.minal.taskflow.models.TaskModel;
import com.minal.taskflow.models.TaskWatcher;
import com.minal.taskflow.models.TaskWatcherId;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskWatcherRepository extends JpaRepository<TaskWatcher, TaskWatcherId> {
    boolean existsByTaskIdAndUserId(UUID taskId, UUID userId);

    @Query("SELECT DISTINCT t FROM TaskModel t " +
            "JOIN t.watchers w JOIN w.user u " +
            "WHERE u.userName = :userName")
    List<TaskModel> findTasksByWatcherUserName(@Param("userName") String userName);
}

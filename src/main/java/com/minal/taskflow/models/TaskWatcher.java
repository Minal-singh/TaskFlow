package com.minal.taskflow.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_watchers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskWatcher {

    @EmbeddedId
    private TaskWatcherId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("taskId")
    @JoinColumn(name = "task_id")
    private TaskModel task;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private UserModel user;

    @Column(nullable = false)
    private LocalDateTime watchedAt = LocalDateTime.now();
}


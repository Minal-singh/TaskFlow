package com.minal.taskflow.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Builder
@Entity
@Data
@Table(name = "tasks")
@NoArgsConstructor
@AllArgsConstructor
public class TaskModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private String priority;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDateTime dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private UserModel user;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<TaskWatcher> watchers = new HashSet<>();

    public void addWatcher(UserModel user) {
        TaskWatcher watcher = TaskWatcher.builder()
                .id(new TaskWatcherId(this.id, user.getId()))
                .task(this)
                .user(user)
                .build();
        watchers.add(watcher);
        user.getWatchedTasks().add(watcher);
    }

    public void removeWatcher(UserModel user) {
        watchers.removeIf(w -> w.getUser().getId().equals(user.getId()));
    }
}

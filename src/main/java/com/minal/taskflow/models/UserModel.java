package com.minal.taskflow.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.*;


@Builder
@Entity
@Data
@Table(name = "users", indexes = {
        @Index(name = "username_idx", columnList = "userName"),
        @Index(name = "email_idx", columnList = "email")
})
@NoArgsConstructor
@AllArgsConstructor
public class UserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String userName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role = "USER";

    @ToString.Exclude
    @OneToMany(mappedBy = "assignee", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<TaskModel> assignedTasks = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "reporter", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TaskModel> reportedTasks = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TaskWatcher> watchedTasks;
}

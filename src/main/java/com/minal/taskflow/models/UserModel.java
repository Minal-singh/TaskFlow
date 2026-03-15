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
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TaskModel> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<TaskWatcher> watchedTasks = new HashSet<>();
}

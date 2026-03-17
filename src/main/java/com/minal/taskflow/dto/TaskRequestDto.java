package com.minal.taskflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequestDto {
    @NonNull
    private String title;

    private String description;

    @NonNull
    private String priority;

    @NonNull
    private String status;

    @NonNull
    private LocalDateTime dueDate;

    // it will be ignored by mapper as there is no matching field in Task entity,
    // but it will be used to find the assignee user and set it in Task entity
    private String assigneeUserName;
}

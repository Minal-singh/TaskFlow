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
}

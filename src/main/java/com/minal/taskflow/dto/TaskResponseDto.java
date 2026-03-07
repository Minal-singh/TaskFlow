package com.minal.taskflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponseDto {
    private UUID id;
    private String title;
    private String description;
    private String priority;
    private String status;
    private LocalDateTime dueDate;
}

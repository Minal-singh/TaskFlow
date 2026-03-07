package com.minal.taskflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskUpdateDto {
    private String title;
    private String description;
    private String priority;
    private String status;
    private LocalDateTime dueDate;
}

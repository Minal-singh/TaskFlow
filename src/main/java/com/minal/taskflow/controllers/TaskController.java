package com.minal.taskflow.controllers;

import com.minal.taskflow.dto.TaskRequestDto;
import com.minal.taskflow.dto.TaskResponseDto;
import com.minal.taskflow.dto.TaskUpdateDto;
import com.minal.taskflow.services.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(taskService.getTaskById(id, userDetails.getUsername()));
    }

    @PostMapping
    public ResponseEntity<TaskResponseDto> createTask(@RequestBody TaskRequestDto taskDto, @AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(taskDto, userDetails.getUsername()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDto> updateTask(@PathVariable UUID id, @RequestBody TaskUpdateDto taskDto, @AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(taskService.updateTask(id, userDetails.getUsername(), taskDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails){
        taskService.deleteTask(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<TaskResponseDto>> getAllTasksForUser(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(taskService.getAllTasksForUser(userDetails.getUsername()));
    }
}

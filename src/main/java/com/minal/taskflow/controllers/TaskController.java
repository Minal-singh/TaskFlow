package com.minal.taskflow.controllers;

import com.minal.taskflow.dto.TaskRequestDto;
import com.minal.taskflow.dto.TaskResponseDto;
import com.minal.taskflow.dto.TaskUpdateDto;
import com.minal.taskflow.dto.WatcherRequestDto;
import com.minal.taskflow.services.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/tasks")
@Tag(name = "Tasks APIs")
@Slf4j
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a task by its ID")
    public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable UUID id,
                                                       @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Fetching task with id: {}", id);
        return ResponseEntity.ok(taskService.getTaskById(id, userDetails.getUsername()));
    }

    @PostMapping
    @Operation(summary = "Create a new task")
    public ResponseEntity<TaskResponseDto> createTask(@RequestBody TaskRequestDto taskDto,
                                                      @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Creating task: {}", taskDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.createTask(taskDto, userDetails.getUsername()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing task")
    public ResponseEntity<TaskResponseDto> updateTask(@PathVariable UUID id, @RequestBody TaskUpdateDto taskDto,
                                                      @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Updating task with id: {}", id);
        return ResponseEntity.ok(taskService.updateTask(id, userDetails.getUsername(), taskDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Deleting task with id: {}", id);
        taskService.deleteTask(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Get all tasks for the all users")
    public ResponseEntity<List<TaskResponseDto>> getAllTasksForUser(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Fetching all tasks");
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(5, TimeUnit.SECONDS))
                .body(taskService.getAllTasks());
    }

    @GetMapping("/assigned")
    public ResponseEntity<List<TaskResponseDto>> getAssignedTasks(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(taskService.getTasksAssignedToUser(userDetails.getUsername()));
    }

    @GetMapping("/reported")
    public ResponseEntity<List<TaskResponseDto>> getReportedTasks(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(taskService.getTasksReportedByUser(userDetails.getUsername()));
    }

    @GetMapping("/watching")
    public ResponseEntity<List<TaskResponseDto>> getWatchingTasks(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(taskService.getTasksWatchedByUser(userDetails.getUsername()));
    }

    @PostMapping("/{id}/watchers")
    public ResponseEntity<Void> addWatcher(@PathVariable UUID id, @RequestBody WatcherRequestDto watcherRequestDto,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        taskService.addWatcher(id, watcherRequestDto.getUserName(), userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/watchers")
    public ResponseEntity<List<String>> getWatchers(@PathVariable UUID id,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(taskService.getWatchers(id, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}/watchers")
    public ResponseEntity<Void> removeWatcher(@PathVariable UUID id, @RequestBody WatcherRequestDto watcherRequestDto,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        taskService.removeWatcher(id, watcherRequestDto.getUserName(), userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}

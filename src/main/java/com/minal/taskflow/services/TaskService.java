package com.minal.taskflow.services;

import com.minal.taskflow.dto.TaskRequestDto;
import com.minal.taskflow.dto.TaskResponseDto;
import com.minal.taskflow.dto.TaskUpdateDto;
import com.minal.taskflow.exceptions.TaskNotFoundException;
import com.minal.taskflow.exceptions.UserNotFoundException;
import com.minal.taskflow.mappers.TaskFlowMapper;
import com.minal.taskflow.models.TaskModel;
import com.minal.taskflow.models.UserModel;
import com.minal.taskflow.repositories.TaskRepository;
import com.minal.taskflow.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskFlowMapper mapper;
    private final UserRepository userRepository;

    public TaskService(
            TaskRepository taskRepository,
            TaskFlowMapper mapper,
            UserRepository userRepository
                      ) {
        this.taskRepository = taskRepository;
        this.mapper = mapper;
        this.userRepository = userRepository;
    }

    @Cacheable(value = "tasks", key = "#id + ':' + #userName")
    public TaskResponseDto getTaskById(UUID id, String userName) {
        log.debug("CACHE MISS: - Fetching task {} for user {}", id, userName);
        log.info("Fetching task with id: {} for user: {}", id, userName);
        UserModel user = userRepository.findByUserName(userName)
                .orElseThrow(UserNotFoundException::new);

        TaskModel task = taskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> {
                    log.error("Task not found with id: {} for user: {}", id, userName);
                    return new TaskNotFoundException();
                });

        log.info("Task retrieved successfully: {}", id);
        return mapper.toTaskDto(task);
    }

    @CachePut(value = "tasks", key = "#result.id + ':' + #userName")
    public TaskResponseDto createTask(TaskRequestDto newTask, String userName) {
        log.info("Creating task for user: {}", userName);
        UserModel user = userRepository.findByUserName(userName)
                .orElseThrow(UserNotFoundException::new);

        TaskModel task = mapper.toTaskEntity(newTask);
        task.setUser(user);
        TaskModel saved = taskRepository.save(task);
        log.info("Task created successfully with id: {} for user: {}", saved.getId(), userName);
        return mapper.toTaskDto(saved);
    }

    @CachePut(value = "tasks", key = "#id + ':' + #userName")
    public TaskResponseDto updateTask(UUID id, String userName, TaskUpdateDto newTask) {
        log.info("Updating task with id: {} for user: {}", id, userName);
        UserModel user = userRepository.findByUserName(userName)
                .orElseThrow(UserNotFoundException::new);

        TaskModel oldTask = taskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> {
                    log.error("Task not found for update with id: {} for user: {}", id, userName);
                    return new TaskNotFoundException();
                });

        if (newTask.getTitle() != null && !newTask.getTitle().isEmpty()) {
            log.debug("Updating task title from '{}' to '{}'", oldTask.getTitle(), newTask.getTitle());
            oldTask.setTitle(newTask.getTitle());
        }
        if (newTask.getDescription() != null && !newTask.getDescription().isEmpty()) {
            log.debug("Updating task description");
            oldTask.setDescription(newTask.getDescription());
        }
        if (newTask.getPriority() != null && !newTask.getPriority().isEmpty()) {
            log.debug("Updating task priority from '{}' to '{}'", oldTask.getPriority(), newTask.getPriority());
            oldTask.setPriority(newTask.getPriority());
        }
        if (newTask.getStatus() != null && !newTask.getStatus().isEmpty()) {
            log.debug("Updating task status from '{}' to '{}'", oldTask.getStatus(), newTask.getStatus());
            oldTask.setStatus(newTask.getStatus());
        }
        if (newTask.getDueDate() != null) {
            log.debug("Updating task due date to {}", newTask.getDueDate());
            oldTask.setDueDate(newTask.getDueDate());
        }
        TaskModel saved = taskRepository.save(oldTask);
        log.info("Task updated successfully: {}", id);
        return mapper.toTaskDto(saved);
    }

    @CacheEvict(value = "tasks", key = "#id + ':' + #userName")
    public void deleteTask(UUID id, String userName) {
        log.info("Deleting task with id: {} for user: {}", id, userName);
        UserModel user = userRepository.findByUserName(userName)
                .orElseThrow(UserNotFoundException::new);

        TaskModel task = taskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> {
                    log.error("Task not found for deletion with id: {} for user: {}", id, userName);
                    return new TaskNotFoundException();
                });

        taskRepository.deleteById(task.getId());
        log.info("Task deleted successfully: {}", id);
    }

    public List<TaskResponseDto> getAllTasksForUser(String userName) {
        log.info("Fetching all tasks for user: {}", userName);
        UserModel user = userRepository.findByUserName(userName)
                .orElseThrow(UserNotFoundException::new);

        List<TaskModel> tasks = taskRepository.findByUser(user);
        log.info("Retrieved {} tasks for user: {}", tasks.size(), userName);
        return mapper.toTaskDtos(tasks);
    }
}

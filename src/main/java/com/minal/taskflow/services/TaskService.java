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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

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

    public TaskResponseDto getTaskById(UUID id, String userName) {
        UserModel user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        TaskModel task = taskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        return mapper.toTaskDto(task);
    }

    public TaskResponseDto createTask(TaskRequestDto newTask, String userName) {
        UserModel user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        TaskModel task = mapper.toTaskEntity(newTask);
        task.setUser(user);
        TaskModel saved = taskRepository.save(task);
        return mapper.toTaskDto(saved);
    }

    public TaskResponseDto updateTask(UUID id, String userName, TaskUpdateDto newTask) {
        UserModel user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        TaskModel oldTask = taskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        if (newTask.getTitle() != null && !newTask.getTitle().isEmpty()) {
            oldTask.setTitle(newTask.getTitle());
        }
        if (newTask.getDescription() != null && !newTask.getDescription().isEmpty()) {
            oldTask.setDescription(newTask.getDescription());
        }
        if (newTask.getPriority() != null && !newTask.getPriority().isEmpty()) {
            oldTask.setPriority(newTask.getPriority());
        }
        if (newTask.getStatus() != null && !newTask.getStatus().isEmpty()) {
            oldTask.setStatus(newTask.getStatus());
        }
        if (newTask.getDueDate() != null) {
            oldTask.setDueDate(newTask.getDueDate());
        }
        TaskModel saved = taskRepository.save(oldTask);
        return mapper.toTaskDto(saved);
    }

    public void deleteTask(UUID id, String userName) {
        UserModel user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        TaskModel task = taskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        taskRepository.deleteById(task.getId());
    }

    public List<TaskResponseDto> getAllTasksForUser(String userName) {
        UserModel user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<TaskModel> tasks = taskRepository.findByUser(user);
        return mapper.toTaskDtos(tasks);
    }
}

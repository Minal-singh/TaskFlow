package com.minal.taskflow.services;

import com.minal.taskflow.dto.TaskRequestDto;
import com.minal.taskflow.dto.TaskResponseDto;
import com.minal.taskflow.dto.TaskUpdateDto;
import com.minal.taskflow.exceptions.TaskNotFoundException;
import com.minal.taskflow.exceptions.UserNotFoundException;
import com.minal.taskflow.mappers.TaskFlowMapper;
import com.minal.taskflow.models.TaskModel;
import com.minal.taskflow.models.TaskWatcher;
import com.minal.taskflow.models.TaskWatcherId;
import com.minal.taskflow.models.UserModel;
import com.minal.taskflow.repositories.TaskRepository;
import com.minal.taskflow.repositories.TaskWatcherRepository;
import com.minal.taskflow.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskFlowMapper mapper;
    private final UserRepository userRepository;
    private final TaskWatcherRepository taskWatcherRepository;

    public TaskService(
            TaskRepository taskRepository,
            TaskFlowMapper mapper,
            UserRepository userRepository,
            TaskWatcherRepository taskWatcherRepository
                      ) {
        this.taskRepository = taskRepository;
        this.mapper = mapper;
        this.userRepository = userRepository;
        this.taskWatcherRepository = taskWatcherRepository;
    }

    @Cacheable(value = "tasks", key = "#id")
    public TaskResponseDto getTaskById(UUID id, String userName) {
        log.debug("CACHE MISS: - Fetching task {}", id);
        log.info("Fetching task with id: {}", id);

        TaskModel task = taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Task not found with id: {}", id);
                    return new TaskNotFoundException();
                });

        log.info("Task retrieved successfully: {}", id);
        return mapper.toTaskDto(task);
    }

    @CachePut(value = "tasks", key = "#result.id")
    public TaskResponseDto createTask(TaskRequestDto newTask, String userName) {
        log.info("Creating task for user: {}", userName);
        UserModel user = userRepository.findByUserName(userName)
                .orElseThrow(UserNotFoundException::new);

        if (newTask.getAssigneeUserName() == null || newTask.getAssigneeUserName().isEmpty()) {
            log.debug("No assignee specified for task, defaulting to reporter: {}", userName);
            newTask.setAssigneeUserName(userName);
        }
        log.debug("Assigning task to user: {}", newTask.getAssigneeUserName());
        UserModel assignee = userRepository.findByUserName(newTask.getAssigneeUserName())
                .orElseThrow(() -> {
                    log.error("Assignee user not found: {}", newTask.getAssigneeUserName());
                    return new UserNotFoundException("Assignee user not found");
                });
        log.debug("Assignee user found: {}", assignee.getUserName());

        TaskModel task = mapper.toTaskEntity(newTask);
        task.setReporter(user);
        task.setAssignee(assignee);
        TaskModel saved = taskRepository.save(task);
        log.info("Task created successfully with id: {} for user: {}", saved.getId(), userName);
        return mapper.toTaskDto(saved);
    }

    @CachePut(value = "tasks", key = "#id")
    public TaskResponseDto updateTask(UUID id, String userName, TaskUpdateDto newTask) {
        log.info("Updating task with id: {} for user: {}", id, userName);
        UserModel user = userRepository.findByUserName(userName)
                .orElseThrow(UserNotFoundException::new);

        TaskModel oldTask = taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Task not found for update with id: {} for user: {}", id, userName);
                    return new TaskNotFoundException();
                });

        if (oldTask.getReporter() != user && oldTask.getAssignee() != user) {
            log.error("Unauthorized update attempt on task id: {} by user: {}", id, userName);
            throw new TaskNotFoundException("Task not found for user");
        }

        if (newTask.getTitle() != null && !newTask.getTitle().isEmpty()) {
            log.debug("Updating task title from '{}' to '{}'", oldTask.getTitle(), newTask.getTitle());
            oldTask.setTitle(newTask.getTitle());
        }
        if (newTask.getDescription() != null && !newTask.getDescription().isEmpty()) {
            log.debug("Updating task description");
            oldTask.setDescription(newTask.getDescription());
        }
        if (newTask.getAssignee() != null && !newTask.getAssignee().isEmpty()) {
            log.debug("Updating task assignee from '{}' to '{}'", oldTask.getAssignee().getUserName(),
                    newTask.getAssignee());
            UserModel newAssignee = userRepository.findByUserName(newTask.getAssignee())
                    .orElseThrow(() -> {
                        log.error("New assignee user not found: {}", newTask.getAssignee());
                        return new UserNotFoundException("New assignee user not found");
                    });
            oldTask.setAssignee(newAssignee);
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

    @CacheEvict(value = "tasks", key = "#id")
    public void deleteTask(UUID id, String userName) {
        log.info("Deleting task with id: {} for user: {}", id, userName);
        UserModel user = userRepository.findByUserName(userName)
                .orElseThrow(UserNotFoundException::new);

        TaskModel task = taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Task not found for deletion with id: {} for user: {}", id, userName);
                    return new TaskNotFoundException();
                });

        if (task.getReporter() != user && task.getAssignee() != user) {
            log.error("Unauthorized delete attempt on task id: {} by user: {}", id, userName);
            throw new TaskNotFoundException("Task not found for user");
        }
        taskRepository.deleteById(task.getId());
        log.info("Task deleted successfully: {}", id);
    }

    public List<TaskResponseDto> getAllTasks() {
        log.info("Fetching all tasks");
        List<TaskModel> tasks = (List<TaskModel>) taskRepository.findAll();
        log.info("Retrieved total {} tasks", tasks.size());
        return mapper.toTaskDtos(tasks);
    }

    public List<TaskResponseDto> getTasksAssignedToUser(String userName) {
        log.info("Fetching tasks assigned to user: {}", userName);
        UserModel user = userRepository.findByUserName(userName)
                .orElseThrow(UserNotFoundException::new);
        List<TaskModel> tasks = taskRepository.findByAssignee(user);
        return mapper.toTaskDtos(tasks);
    }

    public List<TaskResponseDto> getTasksReportedByUser(String userName) {
        log.info("Fetching tasks reported by user: {}", userName);
        UserModel user = userRepository.findByUserName(userName)
                .orElseThrow(UserNotFoundException::new);
        List<TaskModel> tasks = taskRepository.findByReporter(user);
        return mapper.toTaskDtos(tasks);
    }

    public List<TaskResponseDto> getTasksWatchedByUser(String userName) {
        log.info("Fetching tasks watched by user: {}", userName);
        List<TaskModel> tasks = taskWatcherRepository.findTasksByWatcherUserName(userName);
        return mapper.toTaskDtos(tasks);
    }

    public void addWatcher(UUID taskId, String watcherUserName, String requesterUserName) {
        log.info("Adding watcher '{}' to task '{}' by requester '{}'", watcherUserName, taskId, requesterUserName);

        TaskModel task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.error("Task not found for adding watcher with id: {} by requester: {}", taskId,
                            requesterUserName);
                    return new TaskNotFoundException();
                });

        UserModel watcher = userRepository.findByUserName(watcherUserName)
                .orElseThrow(() -> {
                    log.error("Watcher user not found: {}", watcherUserName);
                    return new UserNotFoundException("Watcher user not found");
                });

        if (taskWatcherRepository.existsByTaskIdAndUserId(taskId, watcher.getId())) {
            log.warn("Watcher '{}' is already watching task '{}'", watcherUserName, taskId);
            return; // No need to add again, just return
        }

        TaskWatcherId taskWatcherId = new TaskWatcherId(taskId, watcher.getId());
        TaskWatcher taskWatcher = TaskWatcher.builder()
                .id(taskWatcherId)
                .task(task)
                .user(watcher)
                .watchedAt(LocalDateTime.now())
                .build();
        taskWatcherRepository.save(taskWatcher);
        log.info("Watcher '{}' added successfully to task '{}'", watcherUserName, taskId);
    }

    public List<String> getWatchers(UUID taskId, String requesterUserName) {
        log.info("Fetching watchers for task '{}' by requester '{}'", taskId, requesterUserName);

        List<String> watchers = taskRepository.findWatcherUserNamesByTaskId(taskId);
        log.info("Retrieved {} watchers for task '{}'", watchers.size(), taskId);
        return watchers;
    }

    public void removeWatcher(UUID taskId, String watcherUserName, String requesterUserName) {
        log.info("Removing watcher '{}' from task '{}' by requester '{}'", watcherUserName, taskId, requesterUserName);

        taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.error("Task not found for removing watcher with id: {} by requester: {}", taskId,
                            requesterUserName);
                    return new TaskNotFoundException();
                });

        UserModel watcher = userRepository.findByUserName(watcherUserName)
                .orElseThrow(() -> {
                    log.error("Watcher user not found: {}", watcherUserName);
                    return new UserNotFoundException("Watcher user not found");
                });

        if (!taskWatcherRepository.existsByTaskIdAndUserId(taskId, watcher.getId())) {
            log.warn("Watcher '{}' is not watching task '{}'", watcherUserName, taskId);
            return; // Watcher is not watching, nothing to remove
        }

        TaskWatcherId taskWatcherId = new TaskWatcherId(taskId, watcher.getId());
        taskWatcherRepository.deleteById(taskWatcherId);
        log.info("Watcher '{}' removed successfully from task '{}'", watcherUserName, taskId);
    }
}

package com.minal.taskflow.services;

import com.minal.taskflow.dto.*;
import com.minal.taskflow.exceptions.TaskNotFoundException;
import com.minal.taskflow.exceptions.UserNotFoundException;
import com.minal.taskflow.mappers.TaskFlowMapper;
import com.minal.taskflow.models.*;
import com.minal.taskflow.repositories.TaskRepository;
import com.minal.taskflow.repositories.TaskWatcherRepository;
import com.minal.taskflow.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
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
        UserModel assignee;
        // Avoid redundant lookup if assignee is same as reporter
        if (newTask.getAssigneeUserName().equals(userName)) {
            log.debug("Assignee is same as reporter, reusing user object");
            assignee = user;
        } else {
            assignee = userRepository.findByUserName(newTask.getAssigneeUserName())
                    .orElseThrow(() -> {
                        log.error("Assignee user not found: {}", newTask.getAssigneeUserName());
                        return new UserNotFoundException("Assignee user not found");
                    });
        }
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
            UserModel newAssignee;
            // Avoid redundant lookup if new assignee is same as current user
            if (newTask.getAssignee().equals(userName)) {
                log.debug("New assignee is same as current user, reusing user object");
                newAssignee = user;
            } else {
                newAssignee = userRepository.findByUserName(newTask.getAssignee())
                        .orElseThrow(() -> {
                            log.error("New assignee user not found: {}", newTask.getAssignee());
                            return new UserNotFoundException("New assignee user not found");
                        });
            }
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
        List<TaskModel> tasks = taskRepository.findByAssigneeUserName(userName);
        log.debug("Retrieved {} tasks assigned to user: {}", tasks.size(), userName);
        return mapper.toTaskDtos(tasks);
    }

    public List<TaskResponseDto> getTasksReportedByUser(String userName) {
        log.info("Fetching tasks reported by user: {}", userName);
        List<TaskModel> tasks = taskRepository.findByReporterUserName(userName);
        log.debug("Retrieved {} tasks reported by user: {}", tasks.size(), userName);
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

    public List<CommentResponseDto> getComments(UUID taskId, String requesterUserName) {
        log.info("Fetching comments for task '{}' by requester '{}'", taskId, requesterUserName);

        TaskModel task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.error("Task not found for fetching comments with id: {} by requester: {}", taskId,
                            requesterUserName);
                    return new TaskNotFoundException();
                });

        List<CommentResponseDto> comments = task.getComments().stream()
                .sorted(Comparator.comparing(TaskComment::getCreatedAt).reversed())
                .map(mapper::toCommentDto)
                .toList();
        log.info("Retrieved {} comments for task '{}'", comments.size(), taskId);
        return comments;
    }

    public CommentResponseDto addComment(UUID taskId, CommentRequestDto comment, String commenterUserName) {
        log.info("Adding comment to task '{}' by user '{}'", taskId, commenterUserName);

        TaskModel task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.error("Task not found for adding comment with id: {} by user: {}", taskId,
                            commenterUserName);
                    return new TaskNotFoundException();
                });

        UserModel commenter = userRepository.findByUserName(commenterUserName)
                .orElseThrow(() -> {
                    log.error("Commenter user not found: {}", commenterUserName);
                    return new UserNotFoundException("Commenter user not found");
                });

        TaskComment taskComment = mapper.toCommentEntity(comment);
        taskComment.setTask(task);
        taskComment.setUser(commenter);
        CommentResponseDto commentDto = mapper.toCommentDto(taskComment);
        task.getComments().add(taskComment);
        taskRepository.save(task);
        log.info("Comment added successfully to task '{}'", taskId);
        return commentDto;
    }

    public void deleteComment(UUID taskId, UUID commentId, String requesterUserName) {
        log.info("Deleting comment '{}' from task '{}' by user '{}'", commentId, taskId, requesterUserName);

        TaskModel task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.error("Task not found for deleting comment with id: {} by user: {}", taskId,
                            requesterUserName);
                    return new TaskNotFoundException();
                });

        TaskComment comment = task.getComments().stream()
                .filter(c -> c.getId().equals(commentId))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Comment not found with id: {} in task: {} for user: {}", commentId, taskId,
                            requesterUserName);
                    return new TaskNotFoundException("Comment not found");
                });

        if (comment.getUser() == null || !comment.getUser().getUserName().equals(requesterUserName)) {
            log.error("Unauthorized delete attempt on comment id: {} in task: {} by user: {}", commentId, taskId,
                    requesterUserName);
            throw new TaskNotFoundException("Comment not found for user");
        }

        task.getComments().remove(comment);
        taskRepository.save(task);
        log.info("Comment '{}' deleted successfully from task '{}'", commentId, taskId);
    }

    public CommentResponseDto updateComment(UUID taskId, UUID commentId, CommentRequestDto newComment,
                                            String requesterUserName) {
        log.info("Updating comment '{}' in task '{}' by user '{}'", commentId, taskId, requesterUserName);

        TaskModel task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.error("Task not found for updating comment with id: {} by user: {}", taskId,
                            requesterUserName);
                    return new TaskNotFoundException();
                });

        TaskComment comment = task.getComments().stream()
                .filter(c -> c.getId().equals(commentId))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Comment not found with id: {} in task: {} for user: {}", commentId, taskId,
                            requesterUserName);
                    return new TaskNotFoundException("Comment not found");
                });

        if (comment.getUser() == null || !comment.getUser().getUserName().equals(requesterUserName)) {
            log.error("Unauthorized update attempt on comment id: {} in task: {} by user: {}", commentId, taskId,
                    requesterUserName);
            throw new TaskNotFoundException("Comment not found for user");
        }

        if (!newComment.getText().isEmpty()) {
            log.debug("Updating comment text for comment '{}' in task '{}'", commentId, taskId);
            comment.setText(newComment.getText());
            taskRepository.save(task);
            log.info("Comment '{}' updated successfully in task '{}'", commentId, taskId);
        } else {
            log.warn("No new text provided for updating comment '{}' in task '{}'", commentId, taskId);
        }
        return mapper.toCommentDto(comment);
    }

    public CommentResponseDto getCommentById(UUID taskId, UUID commentId, String requesterUserName) {
        log.info("Fetching comment '{}' from task '{}' by user '{}'", commentId, taskId, requesterUserName);

        TaskModel task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.error("Task not found for fetching comment with id: {} by user: {}", taskId,
                            requesterUserName);
                    return new TaskNotFoundException();
                });

        TaskComment comment = task.getComments().stream()
                .filter(c -> c.getId().equals(commentId))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Comment not found with id: {} in task: {} for user: {}", commentId, taskId,
                            requesterUserName);
                    return new TaskNotFoundException("Comment not found");
                });

        log.info("Comment '{}' retrieved successfully from task '{}'", commentId, taskId);
        return mapper.toCommentDto(comment);
    }
}

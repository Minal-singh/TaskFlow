package com.minal.taskflow.mappers;

import com.minal.taskflow.dto.*;
import com.minal.taskflow.models.TaskComment;
import com.minal.taskflow.models.TaskModel;
import com.minal.taskflow.models.UserModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {TaskFlowMapper.class})
public interface TaskFlowMapper {
    UserResponseDto toUserDto(UserModel user);
    UserModel toUserEntity(UserRequestDto dto);

    @Mapping(target = "assignee", source = "assignee", qualifiedByName = "userToName")
    @Mapping(target = "reporter", source = "reporter", qualifiedByName = "userToName")
    TaskResponseDto toTaskDto(TaskModel task);

    @Named("userToName")
    default String userToName(UserModel user) {
        return user != null ? user.getUserName() : null;
    }

    TaskModel toTaskEntity(TaskRequestDto dto);

    TaskComment toCommentEntity(CommentRequestDto dto);
    CommentResponseDto toCommentDto(TaskComment comment);

    List<UserResponseDto> toUserDtos(List<UserModel> users);
    List<TaskResponseDto> toTaskDtos(List<TaskModel> tasks);
    List<CommentResponseDto> toCommentDtos(Set<TaskComment> tasks);
}

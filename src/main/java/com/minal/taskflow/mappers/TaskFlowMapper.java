package com.minal.taskflow.mappers;

import com.minal.taskflow.dto.*;
import com.minal.taskflow.models.TaskModel;
import com.minal.taskflow.models.UserModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;

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

    List<UserResponseDto> toUserDtos(List<UserModel> users);
    List<TaskResponseDto> toTaskDtos(List<TaskModel> tasks);
}

package com.minal.taskflow.mappers;

import com.minal.taskflow.dto.*;
import com.minal.taskflow.models.TaskModel;
import com.minal.taskflow.models.UserModel;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = {TaskFlowMapper.class})
public interface TaskFlowMapper {
    UserResponseDto toUserDto(UserModel user);
    UserModel toUserEntity(UserRequestDto dto);

    TaskResponseDto toTaskDto(TaskModel task);
    TaskModel toTaskEntity(TaskRequestDto dto);
    void updateTask(TaskUpdateDto dto, @MappingTarget TaskModel task);

    List<UserResponseDto> toUserDtos(List<UserModel> users);
    List<TaskResponseDto> toTaskDtos(List<TaskModel> tasks);
}

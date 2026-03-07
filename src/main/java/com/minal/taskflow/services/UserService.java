package com.minal.taskflow.services;

import com.minal.taskflow.dto.UserRequestDto;
import com.minal.taskflow.dto.UserResponseDto;
import com.minal.taskflow.dto.UserUpdateDto;
import com.minal.taskflow.exceptions.UserAlreadyExistsException;
import com.minal.taskflow.exceptions.UserNotFoundException;
import com.minal.taskflow.mappers.TaskFlowMapper;
import com.minal.taskflow.models.UserModel;
import com.minal.taskflow.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final TaskFlowMapper mapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            TaskFlowMapper mapper, UserRepository userRepository, PasswordEncoder passwordEncoder
    ) {
        this.mapper = mapper;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public UserResponseDto getUser(String query){
        // Try to find by username first
        Optional<UserModel> userByName = userRepository.findByUserName(query);
        if (userByName.isPresent()) {
            return mapper.toUserDto(userByName.get());
        }

        // Then try to find by email
        Optional<UserModel> userByEmail = userRepository.findByEmail(query);
        if (userByEmail.isPresent()) {
            return mapper.toUserDto(userByEmail.get());
        }

        throw new UserNotFoundException("User not found");
    }

    public UserResponseDto createUser(UserRequestDto userDto){
        if (userRepository.findByUserName(userDto.getUserName()).isPresent()) {
            throw new UserAlreadyExistsException("User exists with given username");
        }
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("User exists with given email");
        }
        UserModel user = mapper.toUserEntity(userDto);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRole("USER");
        UserModel saved = userRepository.save(user);
        return mapper.toUserDto(saved);
    }

    public UserResponseDto updateUser(
            String userName, UserUpdateDto userDto
    ){
        UserModel oldUser = userRepository.findByUserName(userName)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (userDto.getUserName() != null && !userDto.getUserName().trim().isEmpty()) {
            String newUserName = userDto.getUserName().trim();
            if (!newUserName.equals(oldUser.getUserName())) {
                if (userRepository.findByUserName(newUserName).isPresent()) {
                    throw new UserAlreadyExistsException("User exists with given username");
                }
                oldUser.setUserName(newUserName);
            }
        }

        if (userDto.getEmail() != null && !userDto.getEmail().trim().isEmpty()) {
            String newEmail = userDto.getEmail().trim();
            if (!newEmail.equalsIgnoreCase(oldUser.getEmail())) {
                if (userRepository.findByEmail(newEmail).isPresent()) {
                    throw new UserAlreadyExistsException("User exists with given email");
                }
                oldUser.setEmail(newEmail);
            }
        }

        if (userDto.getPassword() != null && !userDto.getPassword().trim().isEmpty()) {
            oldUser.setPassword(passwordEncoder.encode(userDto.getPassword().trim()));
        }

        UserModel saved = userRepository.save(oldUser);
        return mapper.toUserDto(saved);
    }

    public void deleteUser(String userName){
        UserModel user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        userRepository.deleteById(user.getId());
    }
}

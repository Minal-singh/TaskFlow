package com.minal.taskflow.services;

import com.minal.taskflow.dto.UserRequestDto;
import com.minal.taskflow.dto.UserResponseDto;
import com.minal.taskflow.dto.UserUpdateDto;
import com.minal.taskflow.exceptions.UserAlreadyExistsException;
import com.minal.taskflow.exceptions.UserNotFoundException;
import com.minal.taskflow.mappers.TaskFlowMapper;
import com.minal.taskflow.models.UserModel;
import com.minal.taskflow.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
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
        log.info("Fetching user by query: {}", query);
        // Try to find by username first
        Optional<UserModel> userByName = userRepository.findByUserName(query);
        if (userByName.isPresent()) {
            log.info("User found by username: {}", query);
            return mapper.toUserDto(userByName.get());
        }

        // Then try to find by email
        Optional<UserModel> userByEmail = userRepository.findByEmail(query);
        if (userByEmail.isPresent()) {
            log.info("User found by email: {}", query);
            return mapper.toUserDto(userByEmail.get());
        }

        log.error("User not found with query: {}", query);
        throw new UserNotFoundException();
    }

    public UserResponseDto createUser(UserRequestDto userDto){
        log.info("Creating user with username: {}", userDto.getUserName());
        if (userRepository.findByUserName(userDto.getUserName()).isPresent()) {
            log.warn("User creation failed: Username already exists - {}", userDto.getUserName());
            throw new UserAlreadyExistsException("User exists with given username");
        }
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            log.warn("User creation failed: Email already exists - {}", userDto.getEmail());
            throw new UserAlreadyExistsException("User exists with given email");
        }
        UserModel user = mapper.toUserEntity(userDto);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRole("USER");
        UserModel saved = userRepository.save(user);
        log.info("User created successfully with username: {}", userDto.getUserName());
        return mapper.toUserDto(saved);
    }

    public UserResponseDto updateUser(
            String userName, UserUpdateDto userDto
    ){
        log.info("Updating user: {}", userName);
        UserModel oldUser = userRepository.findByUserName(userName)
                .orElseThrow(UserNotFoundException::new);

        if (userDto.getUserName() != null && !userDto.getUserName().trim().isEmpty()) {
            String newUserName = userDto.getUserName().trim();
            if (!newUserName.equals(oldUser.getUserName())) {
                if (userRepository.findByUserName(newUserName).isPresent()) {
                    log.warn("User update failed: New username already exists - {}", newUserName);
                    throw new UserAlreadyExistsException("User exists with given username");
                }
                log.debug("Updating username from {} to {}", oldUser.getUserName(), newUserName);
                oldUser.setUserName(newUserName);
            }
        }

        if (userDto.getEmail() != null && !userDto.getEmail().trim().isEmpty()) {
            String newEmail = userDto.getEmail().trim();
            if (!newEmail.equalsIgnoreCase(oldUser.getEmail())) {
                if (userRepository.findByEmail(newEmail).isPresent()) {
                    log.warn("User update failed: New email already exists - {}", newEmail);
                    throw new UserAlreadyExistsException("User exists with given email");
                }
                log.debug("Updating email from {} to {}", oldUser.getEmail(), newEmail);
                oldUser.setEmail(newEmail);
            }
        }

        if (userDto.getPassword() != null && !userDto.getPassword().trim().isEmpty()) {
            log.debug("Updating password for user: {}", userName);
            oldUser.setPassword(passwordEncoder.encode(userDto.getPassword().trim()));
        }

        UserModel saved = userRepository.save(oldUser);
        log.info("User updated successfully: {}", userName);
        return mapper.toUserDto(saved);
    }

    public void deleteUser(String userName){
        log.info("Deleting user: {}", userName);
        UserModel user = userRepository.findByUserName(userName)
                .orElseThrow(UserNotFoundException::new);
        userRepository.deleteById(user.getId());
        log.info("User deleted successfully: {}", userName);
    }
}

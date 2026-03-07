package com.minal.taskflow.controllers;

import com.minal.taskflow.dto.UserResponseDto;
import com.minal.taskflow.dto.UserUpdateDto;
import com.minal.taskflow.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/users")
@Tag(name = "User APIs")
@Slf4j
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Get user by username")
    public ResponseEntity<UserResponseDto> getByUserName(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("GET /users - Fetching user profile for: {}", userDetails.getUsername());
        UserResponseDto user = userService.getUser(userDetails.getUsername());
        log.debug("User profile retrieved successfully for: {}", userDetails.getUsername());
        return ResponseEntity.ok(user);
    }

    @PutMapping
    @Operation(summary = "Update user information")
    public ResponseEntity<UserResponseDto> updateUser(@RequestBody UserUpdateDto userDto, @AuthenticationPrincipal UserDetails userDetails) {
        log.info("PUT /users - Updating user profile for: {}", userDetails.getUsername());
        UserResponseDto user = userService.updateUser(userDetails.getUsername(), userDto);
        log.debug("User profile updated successfully for: {}", userDetails.getUsername());
        return ResponseEntity.ok(user);
    }

    @DeleteMapping
    @Operation(summary = "Delete user account")
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("DELETE /users - Deleting user account for: {}", userDetails.getUsername());
        userService.deleteUser(userDetails.getUsername());
        log.debug("User account deleted successfully for: {}", userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}

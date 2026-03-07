package com.minal.taskflow.controllers;

import com.minal.taskflow.dto.UserResponseDto;
import com.minal.taskflow.dto.UserUpdateDto;
import com.minal.taskflow.services.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

@RestController
@RequestMapping("/users")
@Tag(name = "User APIs")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<UserResponseDto> getByUserName(@AuthenticationPrincipal UserDetails userDetails) {
        UserResponseDto user = userService.getUser(userDetails.getUsername());
        return ResponseEntity.ok(user);
    }

    @PutMapping
    public ResponseEntity<UserResponseDto> updateUser(@RequestBody UserUpdateDto userDto, @AuthenticationPrincipal UserDetails userDetails) {
        UserResponseDto user = userService.updateUser(userDetails.getUsername(), userDto);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteUser(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}

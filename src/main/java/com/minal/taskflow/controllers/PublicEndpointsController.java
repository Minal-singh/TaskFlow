package com.minal.taskflow.controllers;

import com.minal.taskflow.dto.UserLoginDto;
import com.minal.taskflow.dto.UserRequestDto;
import com.minal.taskflow.dto.UserResponseDto;
import com.minal.taskflow.services.JwtBlacklistService;
import com.minal.taskflow.services.UserService;
import com.minal.taskflow.utils.JWTUtils;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Tag(name = "Public APIs")
public class PublicEndpointsController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JWTUtils jwtUtils;
    private final JwtBlacklistService jwtBlacklistService;

    public PublicEndpointsController(UserService userService, AuthenticationManager authenticationManager,
                                     UserDetailsService userDetailsService, JWTUtils jwtUtils,
                                     JwtBlacklistService jwtBlacklistService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtils = jwtUtils;
        this.jwtBlacklistService = jwtBlacklistService;
    }

    @GetMapping("/health-check")
    @Operation(summary = "Check API health status")
    public ResponseEntity<String> healthCheck() {
        log.info("Health check requested");
        return ResponseEntity.ok("Ok. Working fine.");
    }

    @PostMapping("/signup")
    @Operation(summary = "Register a new user")
    public ResponseEntity<UserResponseDto> signup(@RequestBody UserRequestDto userDto) {
        log.info("Signup request received for username: {}", userDto.getUserName());
        UserResponseDto user = userService.createUser(userDto);
        log.info("Signup successful for username: {}", userDto.getUserName());
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/login")
    @Operation(summary = "User login and receive JWT token")
    public ResponseEntity<String> login(@RequestBody UserLoginDto userDto) {
        try {
            log.info("Login attempt for user: {}", userDto.getUserName());
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userDto.getUserName(), userDto.getPassword())
            );
            UserDetails userDetails = userDetailsService.loadUserByUsername(userDto.getUserName());
            String jwt = jwtUtils.generateToken(userDetails.getUsername());
            log.info("Login successful for user: {}", userDto.getUserName());
            return ResponseEntity.ok(jwt);
        } catch (Exception e) {
            log.warn("Login failed for user: {} - {}", userDto.getUserName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect UserName or Password");
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and blacklist current JWT token")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        log.debug("Logout request received.");
        String token = extractToken(request);
        if (token != null) {
            jwtBlacklistService.blacklistToken(token);
            log.info("User logged out");
            Claims claims = jwtUtils.extractAllClaims(token);
            log.debug("Token blacklisted with JTI: {}", claims.get("jti"));
            return ResponseEntity.ok("Logged out successfully");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No token found");
    }

    // Utility method
    private String extractToken(HttpServletRequest request) {
        log.debug("Extracting token from request");
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        log.debug("No valid Authorization header found");
        return null;
    }
}

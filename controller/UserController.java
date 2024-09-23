package com.vulcan.smartcart.controller;

import com.vulcan.smartcart.dto.UserDto;
import com.vulcan.smartcart.exceptions.AlreadyExistsException;
import com.vulcan.smartcart.exceptions.ResourceNotFoundException;
import com.vulcan.smartcart.model.User;
import com.vulcan.smartcart.request.CreateUserRequest;
import com.vulcan.smartcart.request.UserUpdateRequest;
import com.vulcan.smartcart.response.ApiResponse;
import com.vulcan.smartcart.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final IUserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable Long userId) {
        logger.info("Fetching user with ID: {}", userId);
        try {
            User user = userService.getUserById(userId);
            UserDto userDto = userService.convertUserToDto(user);
            logger.info("User fetched successfully: {}", userDto);
            return ResponseEntity.ok(new ApiResponse("Success", userDto));
        } catch (ResourceNotFoundException e) {
            logger.error("User not found: {}", e.getMessage());
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createUser(@RequestBody CreateUserRequest request) {
        logger.info("Creating user: {}", request);
        try {
            User user = userService.createUser(request);
            UserDto userDto = userService.convertUserToDto(user);
            logger.info("User created successfully: {}", userDto);
            return ResponseEntity.ok(new ApiResponse("Create User Success!", userDto));
        } catch (AlreadyExistsException e) {
            logger.error("User already exists: {}", e.getMessage());
            return ResponseEntity.status(CONFLICT).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse> updateUser(@RequestBody UserUpdateRequest request, @PathVariable Long userId) {
        logger.info("Updating user with ID: {}", userId);
        try {
            User user = userService.updateUser(request, userId);
            UserDto userDto = userService.convertUserToDto(user);
            logger.info("User updated successfully: {}", userDto);
            return ResponseEntity.ok(new ApiResponse("Update User Success!", userDto));
        } catch (ResourceNotFoundException e) {
            logger.error("User not found for update: {}", e.getMessage());
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long userId) {
        logger.info("Deleting user with ID: {}", userId);
        try {
            userService.deleteUser(userId);
            logger.info("User deleted successfully: ID {}", userId);
            return ResponseEntity.ok(new ApiResponse("Delete User Success!", null));
        } catch (ResourceNotFoundException e) {
            logger.error("User not found for deletion: {}", e.getMessage());
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }
}


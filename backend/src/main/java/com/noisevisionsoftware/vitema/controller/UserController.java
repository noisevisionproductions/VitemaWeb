package com.noisevisionsoftware.vitema.controller;

import com.noisevisionsoftware.vitema.dto.request.user.UserNoteRequest;
import com.noisevisionsoftware.vitema.dto.request.user.UserUpdateRequest;
import com.noisevisionsoftware.vitema.dto.response.UserResponse;
import com.noisevisionsoftware.vitema.mapper.user.UserMapper;
import com.noisevisionsoftware.vitema.model.user.User;
import com.noisevisionsoftware.vitema.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(
                users.stream()
                        .map(userMapper::toResponse)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String id,
            @Valid @RequestBody UserUpdateRequest request) {
        User user = userService.getUserById(id);
        userMapper.updateUserFromRequest(user, request);
        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(userMapper.toResponse(updatedUser));
    }

    @PatchMapping("/{id}/note")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUserNote(
            @PathVariable String id,
            @RequestBody UserNoteRequest request) {
        User updatedUser = userService.updateUserNote(id, request.getNote());
        return ResponseEntity.ok(userMapper.toResponse(updatedUser));
    }
}
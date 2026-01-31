package com.noisevisionsoftware.vitema.controller;

import com.noisevisionsoftware.vitema.dto.request.user.UserNoteRequest;
import com.noisevisionsoftware.vitema.dto.request.user.UserUpdateRequest;
import com.noisevisionsoftware.vitema.dto.response.UserResponse;
import com.noisevisionsoftware.vitema.mapper.user.UserMapper;
import com.noisevisionsoftware.vitema.model.user.User;
import com.noisevisionsoftware.vitema.model.user.UserRole;
import com.noisevisionsoftware.vitema.security.model.FirebaseUser;
import com.noisevisionsoftware.vitema.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')") // Wpuszczamy obu
    public ResponseEntity<List<UserResponse>> getAllUsers(Authentication authentication) {
        // 1. Pobieramy obiekt użytkownika z SecurityContext (to ten FirebaseUser z AuthService)
        FirebaseUser currentUser = (FirebaseUser) authentication.getPrincipal();

        // 2. Pobieramy ID i Rolę
        String userId = currentUser.getUid();
        UserRole userRole = UserRole.valueOf(currentUser.getRole()); // Zakładam, że role w tokenie są zgodne z Enumem

        // 3. Wołamy serwis z kontekstem
        List<User> users = userService.getUsersBasedOnRole(userId, userRole);

        return ResponseEntity.ok(
                users.stream()
                        .map(userMapper::toResponse)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String id,
            @Valid @RequestBody UserUpdateRequest request) {
        User user = userService.getUserById(id);
        userMapper.updateUserFromRequest(user, request);
        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(userMapper.toResponse(updatedUser));
    }

    @PatchMapping("/{id}/note")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    public ResponseEntity<UserResponse> updateUserNote(
            @PathVariable String id,
            @RequestBody UserNoteRequest request) {
        User updatedUser = userService.updateUserNote(id, request.getNote());
        return ResponseEntity.ok(userMapper.toResponse(updatedUser));
    }

    @GetMapping("/my-clients")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    public ResponseEntity<List<UserResponse>> getMyClients(Principal principal) {
        String trainerId = principal.getName();

        List<User> clients = userService.getClientsForTrainer(trainerId);

        return ResponseEntity.ok(
                clients.stream()
                        .map(userMapper::toResponse)
                        .collect(Collectors.toList())
        );
    }
}
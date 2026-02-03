package com.noisevisionsoftware.vitema.service;

import com.noisevisionsoftware.vitema.exception.NotFoundException;
import com.noisevisionsoftware.vitema.model.user.User;
import com.noisevisionsoftware.vitema.model.user.UserRole;
import com.noisevisionsoftware.vitema.repository.UserRepository;
import com.noisevisionsoftware.vitema.security.model.FirebaseUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    @Cacheable(value = "usersCache", key = "'allUsers'")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getClientsForTrainer(String loggedInTrainerId) {
        return userRepository.findAllByTrainerId(loggedInTrainerId);
    }

    public List<User> getUsersBasedOnRole(String requesterId, UserRole role) {
        if (role == UserRole.ADMIN || role == UserRole.OWNER) {
            return userRepository.findAll();
        } else if (role == UserRole.TRAINER) {
            return userRepository.findAllByTrainerId(requesterId);
        } else {
            return Collections.emptyList();
        }
    }

    @Cacheable(value = "usersCache", key = "#id")
    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
    }

    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof FirebaseUser) {
            return ((FirebaseUser) authentication.getPrincipal()).getUid();
        }
        return null;
    }

    public User getCurrentUser() {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new RuntimeException("Użytkownik nie jest zalogowany");
        }
        return getUserById(currentUserId);
    }

    public boolean isCurrentUserAdminOrOwner() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof FirebaseUser) {
            String role = ((FirebaseUser) authentication.getPrincipal()).getRole();
            return UserRole.ADMIN.name().equals(role) || UserRole.OWNER.name().equals(role);
        }
        return false;
    }

    @Cacheable(value = "userEmailCache", key = "#userId")
    public String getUserEmail(String userId) {
        try {
            User user = getUserById(userId);
            return user.getEmail();
        } catch (Exception e) {
            log.error("Error fetching user email for userId: {}", userId, e);
            return "Nieznany użytkownik";
        }
    }

    @CacheEvict(value = {"usersCache", "userEmailCache", "userRoles"}, allEntries = true)
    public User updateUser(String id, User updatedUser) {
        User existingUser = getUserById(id);

        updatedUser.setId(id);
        updatedUser.setEmail(existingUser.getEmail());
        updatedUser.setRole(existingUser.getRole());
        updatedUser.setCreatedAt(existingUser.getCreatedAt());

        userRepository.save(updatedUser);
        return updatedUser;
    }

    @CacheEvict(value = "usersCache", allEntries = true)
    public User updateUserNote(String id, String note) {
        User user = getUserById(id);
        user.setNote(note);
        userRepository.update(id, user);
        return user;
    }

    public boolean existsById(String userId) {
        try {
            return getUserById(userId) != null;
        } catch (NotFoundException e) {
            return false;
        }
    }

    @Cacheable(value = "userRoles", key = "#userId")
    public UserRole getUserRole(String userId) {
        try {
            User user = getUserById(userId);
            return user.getRole();
        } catch (Exception e) {
            log.error("Error fetching user role for userId: {}", userId, e);
            return UserRole.USER;
        }
    }

    public boolean isAdmin(String userId) {
        return UserRole.ADMIN.equals(getUserRole(userId));
    }
}
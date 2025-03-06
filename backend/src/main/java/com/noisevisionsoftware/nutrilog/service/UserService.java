package com.noisevisionsoftware.nutrilog.service;

import com.noisevisionsoftware.nutrilog.exception.NotFoundException;
import com.noisevisionsoftware.nutrilog.model.user.User;
import com.noisevisionsoftware.nutrilog.model.user.UserRole;
import com.noisevisionsoftware.nutrilog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

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

    @Cacheable(value = "usersCache", key = "#id")
    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
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
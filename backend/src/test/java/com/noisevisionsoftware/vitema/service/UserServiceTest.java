package com.noisevisionsoftware.vitema.service;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.exception.NotFoundException;
import com.noisevisionsoftware.vitema.model.user.User;
import com.noisevisionsoftware.vitema.model.user.UserRole;
import com.noisevisionsoftware.vitema.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private static final String TEST_USER_ID = "testUserId";
    private static final String TEST_EMAIL = "test@example.com";

    private User testUser;

    @BeforeEach
    void setUp() {
        // Przygotowanie przykładowego użytkownika do testów
        testUser = User.builder()
                .id(TEST_USER_ID)
                .email(TEST_EMAIL)
                .role(UserRole.USER)
                .createdAt(Timestamp.now().getSeconds())
                .build();
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Arrange
        List<User> userList = Arrays.asList(testUser, createUser());
        when(userRepository.findAll()).thenReturn(userList);

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertEquals(2, result.size());
        assertEquals(userList, result);
        verify(userRepository).findAll();
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        // Arrange
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserById(TEST_USER_ID);

        // Assert
        assertEquals(testUser, result);
        verify(userRepository).findById(TEST_USER_ID);
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldThrowNotFoundException() {
        // Arrange
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> userService.getUserById(TEST_USER_ID));
        verify(userRepository).findById(TEST_USER_ID);
    }

    @Test
    void getUserEmail_WhenUserExists_ShouldReturnEmail() {
        // Arrange
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        // Act
        String result = userService.getUserEmail(TEST_USER_ID);

        // Assert
        assertEquals(TEST_EMAIL, result);
        verify(userRepository).findById(TEST_USER_ID);
    }

    @Test
    void getUserEmail_WhenUserDoesNotExist_ShouldReturnUnknownUser() {
        // Arrange
        when(userRepository.findById(TEST_USER_ID)).thenThrow(new NotFoundException("User not found"));

        // Act
        String result = userService.getUserEmail(TEST_USER_ID);

        // Assert
        assertEquals("Nieznany użytkownik", result);
    }

    @Test
    void updateUser_WhenUserExists_ShouldUpdateAndReturnUser() {
        // Arrange
        User updatedUser = User.builder()
                .build();

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.updateUser(TEST_USER_ID, updatedUser);

        // Assert
        assertEquals(TEST_USER_ID, result.getId());
        assertEquals(TEST_EMAIL, result.getEmail());
        assertEquals(testUser.getRole(), result.getRole());
        assertEquals(testUser.getCreatedAt(), result.getCreatedAt());

        verify(userRepository).findById(TEST_USER_ID);
        verify(userRepository).save(updatedUser);
    }

    @Test
    void updateUserNote_WhenUserExists_ShouldUpdateAndReturnUser() {
        // Arrange
        String newNote = "Nowa notatka";
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.updateUserNote(TEST_USER_ID, newNote);

        // Assert
        assertEquals(newNote, result.getNote());
        verify(userRepository).findById(TEST_USER_ID);
        verify(userRepository).update(eq(TEST_USER_ID), any(User.class));
    }

    @Test
    void existsById_WhenUserExists_ShouldReturnTrue() {
        // Arrange
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        // Act
        boolean result = userService.existsById(TEST_USER_ID);

        // Assert
        assertTrue(result);
    }

    @Test
    void existsById_WhenUserDoesNotExist_ShouldReturnFalse() {
        // Arrange
        when(userRepository.findById(TEST_USER_ID)).thenThrow(new NotFoundException("User not found"));

        // Act
        boolean result = userService.existsById(TEST_USER_ID);

        // Assert
        assertFalse(result);
    }

    @Test
    void getUserRole_WhenUserExists_ShouldReturnUserRole() {
        // Arrange
        testUser.setRole(UserRole.ADMIN);
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        // Act
        UserRole result = userService.getUserRole(TEST_USER_ID);

        // Assert
        assertEquals(UserRole.ADMIN, result);
    }

    @Test
    void getUserRole_WhenUserDoesNotExist_ShouldReturnDefaultUserRole() {
        // Arrange
        when(userRepository.findById(TEST_USER_ID)).thenThrow(new NotFoundException("User not found"));

        // Act
        UserRole result = userService.getUserRole(TEST_USER_ID);

        // Assert
        assertEquals(UserRole.USER, result);
    }

    @Test
    void isAdmin_WhenUserIsAdmin_ShouldReturnTrue() {
        // Arrange
        testUser.setRole(UserRole.ADMIN);
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        // Act
        boolean result = userService.isAdmin(TEST_USER_ID);

        // Assert
        assertTrue(result);
    }

    @Test
    void isAdmin_WhenUserIsNotAdmin_ShouldReturnFalse() {
        // Arrange
        testUser.setRole(UserRole.USER);
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        // Act
        boolean result = userService.isAdmin(TEST_USER_ID);

        // Assert
        assertFalse(result);
    }

    // Metoda pomocnicza do tworzenia użytkowników testowych
    private User createUser() {
        return User.builder()
                .id("user2")
                .email("user2@example.com")
                .role(UserRole.USER)
                .createdAt(Timestamp.now().getSeconds())
                .build();
    }
}
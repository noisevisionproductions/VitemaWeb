package com.noisevisionsoftware.vitema.controller;

import com.noisevisionsoftware.vitema.dto.request.user.UserNoteRequest;
import com.noisevisionsoftware.vitema.dto.request.user.UserUpdateRequest;
import com.noisevisionsoftware.vitema.dto.response.UserResponse;
import com.noisevisionsoftware.vitema.exception.NotFoundException;
import com.noisevisionsoftware.vitema.mapper.user.UserMapper;
import com.noisevisionsoftware.vitema.model.user.Gender;
import com.noisevisionsoftware.vitema.model.user.User;
import com.noisevisionsoftware.vitema.model.user.UserRole;
import com.noisevisionsoftware.vitema.security.model.FirebaseUser;
import com.noisevisionsoftware.vitema.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private Authentication authentication;

    @Mock
    private Principal principal;

    @Mock
    private FirebaseUser firebaseUser;

    @InjectMocks
    private UserController userController;

    private User testUser;
    private UserResponse testUserResponse;
    private UserUpdateRequest userUpdateRequest;
    private UserNoteRequest userNoteRequest;
    private static final String TEST_USER_ID = "user-123";
    private static final String TEST_TRAINER_ID = "trainer-456";
    private static final String TEST_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(TEST_USER_ID)
                .email(TEST_EMAIL)
                .nickname("TestUser")
                .gender(Gender.MALE)
                .birthDate(946684800000L) // 2000-01-01
                .role(UserRole.USER)
                .note("Test note")
                .createdAt(946684800000L)
                .trainerId(TEST_TRAINER_ID)
                .build();

        testUserResponse = UserResponse.builder()
                .id(TEST_USER_ID)
                .email(TEST_EMAIL)
                .nickname("TestUser")
                .gender(Gender.MALE)
                .birthDate(946684800000L)
                .role(UserRole.USER)
                .note("Test note")
                .createdAt(946684800000L)
                .build();

        userUpdateRequest = new UserUpdateRequest();
        userUpdateRequest.setNickname("UpdatedNickname");
        userUpdateRequest.setGender(Gender.FEMALE);
        userUpdateRequest.setBirthDate(978307200000L); // 2001-01-01
        userUpdateRequest.setNote("Updated note");

        userNoteRequest = new UserNoteRequest();
        userNoteRequest.setNote("New note");
    }

    // GET /api/users - getAllUsers tests

    @Test
    void getAllUsers_WithAdminRole_ShouldReturnAllUsers() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        List<UserResponse> expectedResponses = Arrays.asList(testUserResponse);

        when(authentication.getPrincipal()).thenReturn(firebaseUser);
        when(firebaseUser.getUid()).thenReturn("admin-123");
        when(firebaseUser.getRole()).thenReturn("ADMIN");
        when(userService.getUsersBasedOnRole("admin-123", UserRole.ADMIN)).thenReturn(users);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // Act
        ResponseEntity<List<UserResponse>> response = userController.getAllUsers(authentication);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getId()).isEqualTo(TEST_USER_ID);
        assertThat(response.getBody().get(0).getEmail()).isEqualTo(TEST_EMAIL);

        verify(authentication).getPrincipal();
        verify(firebaseUser).getUid();
        verify(firebaseUser).getRole();
        verify(userService).getUsersBasedOnRole("admin-123", UserRole.ADMIN);
        verify(userMapper).toResponse(testUser);
    }

    @Test
    void getAllUsers_WithTrainerRole_ShouldReturnTrainerClients() {
        // Arrange
        List<User> clients = Arrays.asList(testUser);
        List<UserResponse> expectedResponses = Arrays.asList(testUserResponse);

        when(authentication.getPrincipal()).thenReturn(firebaseUser);
        when(firebaseUser.getUid()).thenReturn(TEST_TRAINER_ID);
        when(firebaseUser.getRole()).thenReturn("TRAINER");
        when(userService.getUsersBasedOnRole(TEST_TRAINER_ID, UserRole.TRAINER)).thenReturn(clients);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // Act
        ResponseEntity<List<UserResponse>> response = userController.getAllUsers(authentication);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);

        verify(userService).getUsersBasedOnRole(TEST_TRAINER_ID, UserRole.TRAINER);
    }

    @Test
    void getAllUsers_WithEmptyList_ShouldReturnOkWithEmptyList() {
        // Arrange
        List<User> emptyUsers = Collections.emptyList();

        when(authentication.getPrincipal()).thenReturn(firebaseUser);
        when(firebaseUser.getUid()).thenReturn("admin-123");
        when(firebaseUser.getRole()).thenReturn("ADMIN");
        when(userService.getUsersBasedOnRole("admin-123", UserRole.ADMIN)).thenReturn(emptyUsers);

        // Act
        ResponseEntity<List<UserResponse>> response = userController.getAllUsers(authentication);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();

        verify(userService).getUsersBasedOnRole("admin-123", UserRole.ADMIN);
        verifyNoInteractions(userMapper);
    }

    @Test
    void getAllUsers_WithMultipleUsers_ShouldReturnAllUsers() {
        // Arrange
        User user2 = User.builder()
                .id("user-456")
                .email("user2@example.com")
                .nickname("User2")
                .role(UserRole.USER)
                .build();

        UserResponse response2 = UserResponse.builder()
                .id("user-456")
                .email("user2@example.com")
                .nickname("User2")
                .role(UserRole.USER)
                .build();

        List<User> users = Arrays.asList(testUser, user2);

        when(authentication.getPrincipal()).thenReturn(firebaseUser);
        when(firebaseUser.getUid()).thenReturn("admin-123");
        when(firebaseUser.getRole()).thenReturn("ADMIN");
        when(userService.getUsersBasedOnRole("admin-123", UserRole.ADMIN)).thenReturn(users);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);
        when(userMapper.toResponse(user2)).thenReturn(response2);

        // Act
        ResponseEntity<List<UserResponse>> response = userController.getAllUsers(authentication);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);

        verify(userMapper, times(2)).toResponse(any(User.class));
    }

    // GET /api/users/{id} - getUserById tests

    @Test
    void getUserById_WithValidId_ShouldReturnOkWithUser() {
        // Arrange
        when(userService.getUserById(TEST_USER_ID)).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // Act
        ResponseEntity<UserResponse> response = userController.getUserById(TEST_USER_ID);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(TEST_USER_ID);
        assertThat(response.getBody().getEmail()).isEqualTo(TEST_EMAIL);

        verify(userService).getUserById(TEST_USER_ID);
        verify(userMapper).toResponse(testUser);
    }

    @Test
    void getUserById_WithNonExistentId_ShouldThrowNotFoundException() {
        // Arrange
        String nonExistentId = "non-existent-id";
        when(userService.getUserById(nonExistentId))
                .thenThrow(new NotFoundException("User not found with id: " + nonExistentId));

        // Act & Assert
        try {
            userController.getUserById(nonExistentId);
        } catch (NotFoundException e) {
            assertThat(e.getMessage()).contains("User not found");
        }

        verify(userService).getUserById(nonExistentId);
        verifyNoInteractions(userMapper);
    }

    // PUT /api/users/{id} - updateUser tests

    @Test
    void updateUser_WithValidRequest_ShouldReturnOkWithUpdatedUser() {
        // Arrange
        User updatedUser = User.builder()
                .id(TEST_USER_ID)
                .email(TEST_EMAIL)
                .nickname("UpdatedNickname")
                .gender(Gender.FEMALE)
                .birthDate(978307200000L)
                .note("Updated note")
                .role(UserRole.USER)
                .createdAt(946684800000L)
                .build();

        UserResponse updatedResponse = UserResponse.builder()
                .id(TEST_USER_ID)
                .email(TEST_EMAIL)
                .nickname("UpdatedNickname")
                .gender(Gender.FEMALE)
                .birthDate(978307200000L)
                .note("Updated note")
                .role(UserRole.USER)
                .createdAt(946684800000L)
                .build();

        when(userService.getUserById(TEST_USER_ID)).thenReturn(testUser);
        doNothing().when(userMapper).updateUserFromRequest(testUser, userUpdateRequest);
        when(userService.updateUser(TEST_USER_ID, testUser)).thenReturn(updatedUser);
        when(userMapper.toResponse(updatedUser)).thenReturn(updatedResponse);

        // Act
        ResponseEntity<UserResponse> response = userController.updateUser(TEST_USER_ID, userUpdateRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNickname()).isEqualTo("UpdatedNickname");
        assertThat(response.getBody().getGender()).isEqualTo(Gender.FEMALE);
        assertThat(response.getBody().getNote()).isEqualTo("Updated note");

        verify(userService).getUserById(TEST_USER_ID);
        verify(userMapper).updateUserFromRequest(testUser, userUpdateRequest);
        verify(userService).updateUser(TEST_USER_ID, testUser);
        verify(userMapper).toResponse(updatedUser);
    }

    @Test
    void updateUser_WithPartialUpdate_ShouldReturnOkWithPartiallyUpdatedUser() {
        // Arrange
        UserUpdateRequest partialRequest = new UserUpdateRequest();
        partialRequest.setNickname("PartialUpdate");
        // gender, birthDate, note are null

        User updatedUser = User.builder()
                .id(TEST_USER_ID)
                .email(TEST_EMAIL)
                .nickname("PartialUpdate")
                .gender(Gender.MALE) // unchanged
                .birthDate(946684800000L) // unchanged
                .note("Test note") // unchanged
                .role(UserRole.USER)
                .createdAt(946684800000L)
                .build();

        UserResponse updatedResponse = UserResponse.builder()
                .id(TEST_USER_ID)
                .email(TEST_EMAIL)
                .nickname("PartialUpdate")
                .gender(Gender.MALE)
                .birthDate(946684800000L)
                .note("Test note")
                .role(UserRole.USER)
                .createdAt(946684800000L)
                .build();

        when(userService.getUserById(TEST_USER_ID)).thenReturn(testUser);
        doNothing().when(userMapper).updateUserFromRequest(testUser, partialRequest);
        when(userService.updateUser(TEST_USER_ID, testUser)).thenReturn(updatedUser);
        when(userMapper.toResponse(updatedUser)).thenReturn(updatedResponse);

        // Act
        ResponseEntity<UserResponse> response = userController.updateUser(TEST_USER_ID, partialRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNickname()).isEqualTo("PartialUpdate");

        verify(userService).getUserById(TEST_USER_ID);
        verify(userMapper).updateUserFromRequest(testUser, partialRequest);
    }

    @Test
    void updateUser_WithNonExistentId_ShouldThrowNotFoundException() {
        // Arrange
        String nonExistentId = "non-existent-id";
        when(userService.getUserById(nonExistentId))
                .thenThrow(new NotFoundException("User not found with id: " + nonExistentId));

        // Act & Assert
        try {
            userController.updateUser(nonExistentId, userUpdateRequest);
        } catch (NotFoundException e) {
            assertThat(e.getMessage()).contains("User not found");
        }

        verify(userService).getUserById(nonExistentId);
        verify(userMapper, never()).updateUserFromRequest(any(), any());
        verify(userService, never()).updateUser(anyString(), any());
    }

    // PATCH /api/users/{id}/note - updateUserNote tests

    @Test
    void updateUserNote_WithValidRequest_ShouldReturnOkWithUpdatedUser() {
        // Arrange
        User updatedUser = User.builder()
                .id(TEST_USER_ID)
                .email(TEST_EMAIL)
                .nickname("TestUser")
                .note("New note")
                .role(UserRole.USER)
                .createdAt(946684800000L)
                .build();

        UserResponse updatedResponse = UserResponse.builder()
                .id(TEST_USER_ID)
                .email(TEST_EMAIL)
                .nickname("TestUser")
                .note("New note")
                .role(UserRole.USER)
                .createdAt(946684800000L)
                .build();

        when(userService.updateUserNote(TEST_USER_ID, "New note")).thenReturn(updatedUser);
        when(userMapper.toResponse(updatedUser)).thenReturn(updatedResponse);

        // Act
        ResponseEntity<UserResponse> response = userController.updateUserNote(TEST_USER_ID, userNoteRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNote()).isEqualTo("New note");

        verify(userService).updateUserNote(TEST_USER_ID, "New note");
        verify(userMapper).toResponse(updatedUser);
    }

    @Test
    void updateUserNote_WithEmptyNote_ShouldReturnOkWithEmptyNote() {
        // Arrange
        UserNoteRequest emptyNoteRequest = new UserNoteRequest();
        emptyNoteRequest.setNote("");

        User updatedUser = User.builder()
                .id(TEST_USER_ID)
                .email(TEST_EMAIL)
                .note("")
                .role(UserRole.USER)
                .build();

        UserResponse updatedResponse = UserResponse.builder()
                .id(TEST_USER_ID)
                .email(TEST_EMAIL)
                .note("")
                .role(UserRole.USER)
                .build();

        when(userService.updateUserNote(TEST_USER_ID, "")).thenReturn(updatedUser);
        when(userMapper.toResponse(updatedUser)).thenReturn(updatedResponse);

        // Act
        ResponseEntity<UserResponse> response = userController.updateUserNote(TEST_USER_ID, emptyNoteRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNote()).isEmpty();

        verify(userService).updateUserNote(TEST_USER_ID, "");
    }

    @Test
    void updateUserNote_WithNullNote_ShouldReturnOkWithNullNote() {
        // Arrange
        UserNoteRequest nullNoteRequest = new UserNoteRequest();
        nullNoteRequest.setNote(null);

        User updatedUser = User.builder()
                .id(TEST_USER_ID)
                .email(TEST_EMAIL)
                .note(null)
                .role(UserRole.USER)
                .build();

        UserResponse updatedResponse = UserResponse.builder()
                .id(TEST_USER_ID)
                .email(TEST_EMAIL)
                .note(null)
                .role(UserRole.USER)
                .build();

        when(userService.updateUserNote(TEST_USER_ID, null)).thenReturn(updatedUser);
        when(userMapper.toResponse(updatedUser)).thenReturn(updatedResponse);

        // Act
        ResponseEntity<UserResponse> response = userController.updateUserNote(TEST_USER_ID, nullNoteRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        verify(userService).updateUserNote(TEST_USER_ID, null);
    }

    @Test
    void updateUserNote_WithNonExistentId_ShouldThrowNotFoundException() {
        // Arrange
        String nonExistentId = "non-existent-id";
        when(userService.updateUserNote(nonExistentId, "New note"))
                .thenThrow(new NotFoundException("User not found with id: " + nonExistentId));

        // Act & Assert
        try {
            userController.updateUserNote(nonExistentId, userNoteRequest);
        } catch (NotFoundException e) {
            assertThat(e.getMessage()).contains("User not found");
        }

        verify(userService).updateUserNote(nonExistentId, "New note");
        verifyNoInteractions(userMapper);
    }

    // GET /api/users/my-clients - getMyClients tests

    @Test
    void getMyClients_WithValidTrainer_ShouldReturnOkWithClients() {
        // Arrange
        List<User> clients = Arrays.asList(testUser);
        List<UserResponse> expectedResponses = Arrays.asList(testUserResponse);

        when(principal.getName()).thenReturn(TEST_TRAINER_ID);
        when(userService.getClientsForTrainer(TEST_TRAINER_ID)).thenReturn(clients);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // Act
        ResponseEntity<List<UserResponse>> response = userController.getMyClients(principal);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getId()).isEqualTo(TEST_USER_ID);

        verify(principal).getName();
        verify(userService).getClientsForTrainer(TEST_TRAINER_ID);
        verify(userMapper).toResponse(testUser);
    }

    @Test
    void getMyClients_WithNoClients_ShouldReturnOkWithEmptyList() {
        // Arrange
        List<User> emptyClients = Collections.emptyList();

        when(principal.getName()).thenReturn(TEST_TRAINER_ID);
        when(userService.getClientsForTrainer(TEST_TRAINER_ID)).thenReturn(emptyClients);

        // Act
        ResponseEntity<List<UserResponse>> response = userController.getMyClients(principal);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();

        verify(userService).getClientsForTrainer(TEST_TRAINER_ID);
        verifyNoInteractions(userMapper);
    }

    @Test
    void getMyClients_WithMultipleClients_ShouldReturnAllClients() {
        // Arrange
        User client2 = User.builder()
                .id("client-456")
                .email("client2@example.com")
                .nickname("Client2")
                .role(UserRole.USER)
                .trainerId(TEST_TRAINER_ID)
                .build();

        UserResponse response2 = UserResponse.builder()
                .id("client-456")
                .email("client2@example.com")
                .nickname("Client2")
                .role(UserRole.USER)
                .build();

        List<User> clients = Arrays.asList(testUser, client2);

        when(principal.getName()).thenReturn(TEST_TRAINER_ID);
        when(userService.getClientsForTrainer(TEST_TRAINER_ID)).thenReturn(clients);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);
        when(userMapper.toResponse(client2)).thenReturn(response2);

        // Act
        ResponseEntity<List<UserResponse>> response = userController.getMyClients(principal);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);

        verify(userMapper, times(2)).toResponse(any(User.class));
    }
}

package com.noisevisionsoftware.nutrilog.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.github.benmanes.caffeine.cache.Cache;
import com.noisevisionsoftware.nutrilog.model.user.UserRole;
import com.noisevisionsoftware.nutrilog.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private Firestore firestore;

    @Mock
    private Cache<String, String> userEmailCache;

    @Mock
    private DocumentReference documentReference;

    @Mock
    private DocumentSnapshot documentSnapshot;

    @Mock
    private ApiFuture<DocumentSnapshot> future;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CacheManager cacheManager;

    @InjectMocks
    private UserService userService;

    private static final String TEST_USER_ID = "testUserId";
    private static final String TEST_EMAIL = "test@example.com";

    @Test
    void getUserEmail_WhenEmailInCache_ShouldReturnCachedEmail() {
        // Arrange
        when(userEmailCache.getIfPresent(TEST_USER_ID)).thenReturn(TEST_EMAIL);

        // Act
        String result = userService.getUserEmail(TEST_USER_ID);

        // Assert
        assertEquals(TEST_EMAIL, result);
        verify(userEmailCache).getIfPresent(TEST_USER_ID);
        verifyNoInteractions(firestore);
    }


    @Test
    void getUserEmail_WhenEmailNotInCache_ShouldFetchFromFirestore() throws Exception {
        // Arrange
        when(userEmailCache.getIfPresent(TEST_USER_ID)).thenReturn(null);
        when(firestore.collection("users")).thenReturn(mock(CollectionReference.class));
        when(firestore.collection("users").document(TEST_USER_ID)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(future);
        when(future.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.getString("email")).thenReturn(TEST_EMAIL);

        // Act
        String result = userService.getUserEmail(TEST_USER_ID);

        // Assert
        assertEquals(TEST_EMAIL, result);
        verify(userEmailCache).getIfPresent(TEST_USER_ID);
        verify(userEmailCache).put(TEST_USER_ID, TEST_EMAIL);
        verify(documentSnapshot).getString("email");
    }

    @Test
    void getUserEmail_WhenUserDoesNotExist_ShouldReturnUnknownUser() throws Exception {
        // Arrange
        when(userEmailCache.getIfPresent(TEST_USER_ID)).thenReturn(null);
        when(firestore.collection("users")).thenReturn(mock(CollectionReference.class));
        when(firestore.collection("users").document(TEST_USER_ID)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(future);
        when(future.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(false);

        // Act
        String result = userService.getUserEmail(TEST_USER_ID);

        // Assert
        assertEquals("Nieznany użytkownik", result);
    }

    @Test
    void existsById_WhenUserExists_ShouldReturnTrue() throws Exception {
        // Arrange
        when(firestore.collection("users")).thenReturn(mock(CollectionReference.class));
        when(firestore.collection("users").document(TEST_USER_ID)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(future);
        when(future.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);

        // Act
        boolean result = userService.existsById(TEST_USER_ID);

        // Assert
        assertTrue(result);
    }

    @Test
    void existsById_WhenUserDoesNotExist_ShouldReturnFalse() throws Exception {
        // Arrange
        when(firestore.collection("users")).thenReturn(mock(CollectionReference.class));
        when(firestore.collection("users").document(TEST_USER_ID)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(future);
        when(future.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(false);

        // Act
        boolean result = userService.existsById(TEST_USER_ID);

        // Assert
        assertFalse(result);
    }

    @Test
    void existsById_WhenExceptionOccurs_ShouldReturnFalse() throws Exception {
        // Arrange
        when(firestore.collection("users")).thenReturn(mock(CollectionReference.class));
        when(firestore.collection("users").document(TEST_USER_ID)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(future);
        when(future.get()).thenThrow(new RuntimeException("Database error"));

        // Act
        boolean result = userService.existsById(TEST_USER_ID);

        // Assert
        assertFalse(result);
    }

    @Test
    void getUserRole_WhenUserExists_ShouldReturnUserRole() throws Exception {
        // Arrange
        when(firestore.collection("users")).thenReturn(mock(CollectionReference.class));
        when(firestore.collection("users").document(TEST_USER_ID)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(future);
        when(future.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.getString("role")).thenReturn("ADMIN");

        // Act
        UserRole result = userService.getUserRole(TEST_USER_ID);

        // Assert
        assertEquals(UserRole.ADMIN, result);
    }

    @Test
    void getUserRole_WhenUserDoesNotExist_ShouldReturnDefaultUserRole() throws Exception {
        // Arrange
        when(firestore.collection("users")).thenReturn(mock(CollectionReference.class));
        when(firestore.collection("users").document(TEST_USER_ID)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(future);
        when(future.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(false);

        // Act
        UserRole result = userService.getUserRole(TEST_USER_ID);

        // Assert
        assertEquals(UserRole.USER, result);
    }

    @Test
    void isAdmin_WhenUserIsAdmin_ShouldReturnTrue() throws Exception {
        // Arrange
        when(firestore.collection("users")).thenReturn(mock(CollectionReference.class));
        when(firestore.collection("users").document(TEST_USER_ID)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(future);
        when(future.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.getString("role")).thenReturn("ADMIN");

        // Act
        boolean result = userService.isAdmin(TEST_USER_ID);

        // Assert
        assertTrue(result);
    }

    @Test
    void isAdmin_WhenUserIsNotAdmin_ShouldReturnFalse() throws Exception {
        // Arrange
        when(firestore.collection("users")).thenReturn(mock(CollectionReference.class));
        when(firestore.collection("users").document(TEST_USER_ID)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(future);
        when(future.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.getString("role")).thenReturn("USER");

        // Act
        boolean result = userService.isAdmin(TEST_USER_ID);

        // Assert
        assertFalse(result);
    }
}
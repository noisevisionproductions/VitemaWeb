package com.noisevisionsoftware.vitema.config.firebase;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FirebaseConfigTest {

    private FirebaseConfig firebaseConfig;
    private static final String TEST_CONFIG_PATH = "test-firebase-config.json";

    @BeforeEach
    void setUp() {
        firebaseConfig = new FirebaseConfig();
        ReflectionTestUtils.setField(firebaseConfig, "firebaseConfigPath", TEST_CONFIG_PATH);
    }

    @AfterEach
    void tearDown() {
        // Clean up FirebaseApp instances if any were created
        try {
            FirebaseApp.getInstance().delete();
        } catch (Exception e) {
            // Ignore if no instance exists
        }
    }

    @Test
    void firebaseApp_WhenAppsNotEmpty_ShouldReturnExistingInstance() throws IOException {
        // Arrange
        FirebaseApp existingApp = mock(FirebaseApp.class);

        try (MockedStatic<FirebaseApp> firebaseAppMock = mockStatic(FirebaseApp.class)) {
            firebaseAppMock.when(FirebaseApp::getApps).thenReturn(Collections.singletonList(existingApp));
            firebaseAppMock.when(FirebaseApp::getInstance).thenReturn(existingApp);

            // Act
            FirebaseApp result = firebaseConfig.firebaseApp();

            // Assert
            assertNotNull(result);
            assertEquals(existingApp, result);
            firebaseAppMock.verify(FirebaseApp::getInstance);
            firebaseAppMock.verify(() -> FirebaseApp.initializeApp(any(FirebaseOptions.class)), never());
        }
    }

    @Test
    void firebaseAuth_ShouldReturnFirebaseAuthInstance() {
        // Arrange
        FirebaseApp mockFirebaseApp = mock(FirebaseApp.class);
        FirebaseAuth mockFirebaseAuth = mock(FirebaseAuth.class);

        try (MockedStatic<FirebaseAuth> firebaseAuthMock = mockStatic(FirebaseAuth.class)) {
            firebaseAuthMock.when(() -> FirebaseAuth.getInstance(mockFirebaseApp))
                    .thenReturn(mockFirebaseAuth);

            // Act
            FirebaseAuth result = firebaseConfig.firebaseAuth(mockFirebaseApp);

            // Assert
            assertNotNull(result);
            assertEquals(mockFirebaseAuth, result);
            firebaseAuthMock.verify(() -> FirebaseAuth.getInstance(mockFirebaseApp));
        }
    }

    @Test
    void firestore_ShouldCallFirestoreClientGetFirestore() throws IOException {
        // Arrange
        FirebaseApp mockFirebaseApp = mock(FirebaseApp.class);
        Firestore mockFirestore = mock(Firestore.class);

        try (MockedStatic<FirestoreClient> firestoreClientMock = mockStatic(FirestoreClient.class)) {
            firestoreClientMock.when(() -> FirestoreClient.getFirestore(any(FirebaseApp.class)))
                    .thenReturn(mockFirestore);

            // Mock firebaseApp() to return our mock
            try (MockedStatic<FirebaseApp> firebaseAppMock = mockStatic(FirebaseApp.class)) {
                firebaseAppMock.when(FirebaseApp::getApps).thenReturn(Collections.singletonList(mockFirebaseApp));
                firebaseAppMock.when(FirebaseApp::getInstance).thenReturn(mockFirebaseApp);

                // Act
                Firestore result = firebaseConfig.firestore();

                // Assert
                assertNotNull(result);
                assertEquals(mockFirestore, result);
                firestoreClientMock.verify(() -> FirestoreClient.getFirestore(mockFirebaseApp));
            }
        }
    }

    @Test
    void firebaseConfig_ShouldHaveCorrectConfigPath() {
        // Arrange & Act
        String configPath = (String) ReflectionTestUtils.getField(firebaseConfig, "firebaseConfigPath");

        // Assert
        assertEquals(TEST_CONFIG_PATH, configPath);
    }
}

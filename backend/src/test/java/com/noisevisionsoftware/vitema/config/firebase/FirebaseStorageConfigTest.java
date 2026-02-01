package com.noisevisionsoftware.vitema.config.firebase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FirebaseStorageConfigTest {

    private FirebaseStorageConfig firebaseStorageConfig;
    private static final String TEST_SERVICE_ACCOUNT_PATH = "test-service-account.json";
    private static final String TEST_BUCKET_NAME = "test-project.appspot.com";

    @BeforeEach
    void setUp() {
        firebaseStorageConfig = new FirebaseStorageConfig();
        ReflectionTestUtils.setField(firebaseStorageConfig, "serviceAccountPath", TEST_SERVICE_ACCOUNT_PATH);
        ReflectionTestUtils.setField(firebaseStorageConfig, "bucketName", TEST_BUCKET_NAME);
    }

    @Test
    void extractProjectId_WhenBucketNameContainsAppspotCom_ShouldExtractProjectId() {
        // Arrange
        String bucketName = "my-project.appspot.com";

        // Act - use reflection to access private method
        String result = ReflectionTestUtils.invokeMethod(
                firebaseStorageConfig, "extractProjectId", bucketName);

        // Assert
        assertEquals("my-project", result);
    }

    @Test
    void extractProjectId_WhenBucketNameIsNull_ShouldReturnUnknownProject() {
        // Act - use reflection to access private method
        String result = ReflectionTestUtils.invokeMethod(
                firebaseStorageConfig, "extractProjectId", (String) null);

        // Assert
        assertEquals("unknown-project", result);
    }

    @Test
    void extractProjectId_WhenBucketNameDoesNotContainAppspotCom_ShouldReturnBucketName() {
        // Arrange
        String bucketName = "custom-bucket-name";

        // Act - use reflection to access private method
        String result = ReflectionTestUtils.invokeMethod(
                firebaseStorageConfig, "extractProjectId", bucketName);

        // Assert
        assertEquals("custom-bucket-name", result);
    }

    @Test
    void extractProjectId_WhenBucketNameIsEmpty_ShouldReturnEmptyString() {
        // Arrange
        String bucketName = "";

        // Act - use reflection to access private method
        String result = ReflectionTestUtils.invokeMethod(
                firebaseStorageConfig, "extractProjectId", bucketName);

        // Assert
        assertEquals("", result);
    }

    @Test
    void extractProjectId_WhenBucketNameHasMultipleAppspotCom_ShouldReplaceAll() {
        // Arrange
        String bucketName = "my-project.appspot.com.backup.appspot.com";

        // Act - use reflection to access private method
        String result = ReflectionTestUtils.invokeMethod(
                firebaseStorageConfig, "extractProjectId", bucketName);

        // Assert - replace() replaces all occurrences
        assertEquals("my-project.backup", result);
    }

    @Test
    void firebaseStorageConfig_ShouldHaveCorrectServiceAccountPath() {
        // Arrange & Act
        String serviceAccountPath = (String) ReflectionTestUtils.getField(
                firebaseStorageConfig, "serviceAccountPath");

        // Assert
        assertEquals(TEST_SERVICE_ACCOUNT_PATH, serviceAccountPath);
    }

    @Test
    void firebaseStorageConfig_ShouldHaveCorrectBucketName() {
        // Arrange & Act
        String bucketName = (String) ReflectionTestUtils.getField(
                firebaseStorageConfig, "bucketName");

        // Assert
        assertEquals(TEST_BUCKET_NAME, bucketName);
    }
}

package com.noisevisionsoftware.vitema.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.noisevisionsoftware.vitema.service.firebase.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    @Mock
    private Storage storage;

    @Mock
    private MultipartFile multipartFile;

    @Mock
    private Blob blob;

    @InjectMocks
    private FileStorageService fileStorageService;

    private final String testBucketName = "test-bucket";
    private final String testUserId = "test-user";
    private final String testFileName = "test-file.xlsx";
    private final String testContentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private final byte[] testFileContent = "test content".getBytes();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fileStorageService, "bucketName", testBucketName);
    }

    @Test
    void uploadFile_SuccessfulUpload_ReturnsMediaLink() throws IOException {
        // Arrange
        Storage.BucketGetOption bucketGetOption = Storage.BucketGetOption.fields();
        Bucket mockBucket = mock(Bucket.class);
        when(storage.get(eq(testBucketName), any(Storage.BucketGetOption.class))).thenReturn(mockBucket);
        when(mockBucket.exists()).thenReturn(true);

        when(multipartFile.getOriginalFilename()).thenReturn(testFileName);
        when(multipartFile.getContentType()).thenReturn(testContentType);
        when(multipartFile.getBytes()).thenReturn(testFileContent);
        when(storage.create(any(BlobInfo.class), eq(testFileContent))).thenReturn(blob);

        String signedUrlString = "https://storage.googleapis.com/test-signed-url";
        when(blob.signUrl(eq(365L), eq(TimeUnit.DAYS))).thenReturn(URI.create(signedUrlString).toURL());

        // Act
        String result = fileStorageService.uploadFile(multipartFile, testUserId);

        // Assert
        assertEquals(signedUrlString, result, "Podpisany URL powinien zostać zwrócony poprawnie");

        // Capture arguments passed to storage.create()
        ArgumentCaptor<BlobInfo> blobInfoCaptor = ArgumentCaptor.forClass(BlobInfo.class);
        verify(storage).create(blobInfoCaptor.capture(), eq(testFileContent));

        // Verify BlobInfo was properly configured
        BlobInfo capturedBlobInfo = blobInfoCaptor.getValue();
        assertEquals(testBucketName, capturedBlobInfo.getBucket(), "Nazwa bucketu powinna być poprawna");
        assertTrue(capturedBlobInfo.getName().startsWith("diets/" + testUserId + "/"),
                "Ścieżka pliku powinna zawierać katalog użytkownika");
        assertTrue(capturedBlobInfo.getName().endsWith(".xlsx"),
                "Ścieżka pliku powinna zachować rozszerzenie");
        assertEquals(testContentType, capturedBlobInfo.getContentType(),
                "Typ zawartości powinien być ustawiony poprawnie");
    }

    @Test
    void uploadFile_GetBytesError_ThrowsException() throws IOException {
        // Arrange
        Bucket mockBucket = mock(Bucket.class);
        when(storage.get(eq(testBucketName), any(Storage.BucketGetOption.class))).thenReturn(mockBucket);
        when(mockBucket.exists()).thenReturn(true);

        when(multipartFile.getOriginalFilename()).thenReturn(testFileName);
        when(multipartFile.getContentType()).thenReturn(testContentType);

        IOException ioException = new IOException("Błąd odczytu bajtów pliku");
        when(multipartFile.getBytes()).thenThrow(ioException);

        // Act & Assert
        Exception exception = assertThrows(IOException.class, () ->
                        fileStorageService.uploadFile(multipartFile, testUserId),
                "Metoda powinna ponownie rzucić IOException");

        assertEquals(ioException, exception);
        verify(storage, never()).create(any(BlobInfo.class), any());
    }

    @Test
    void generateFileName_FileWithExtension_ReturnsNameWithExtension() throws Exception {
        // Arrange - use reflection to access private method
        java.lang.reflect.Method generateFileNameMethod = FileStorageService.class
                .getDeclaredMethod("generateFileName", String.class);
        generateFileNameMethod.setAccessible(true);

        // Act
        String name = (String) generateFileNameMethod.invoke(fileStorageService, "file.pdf");

        // Assert
        assertTrue(name.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.pdf"),
                "Name should contain UUID and .pdf extension");
    }

    @Test
    void generateFileName_FileWithoutExtension_ReturnsNameWithoutExtension() throws Exception {
        // Arrange - use reflection to access private method
        java.lang.reflect.Method generateFileNameMethod = FileStorageService.class
                .getDeclaredMethod("generateFileName", String.class);
        generateFileNameMethod.setAccessible(true);

        // Act
        String name = (String) generateFileNameMethod.invoke(fileStorageService, "fileWithoutExtension");

        // Assert
        assertTrue(name.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"),
                "Name should contain only UUID without extension");
    }

    @Test
    void generateFileName_NullFileName_ReturnsNameWithoutExtension() throws Exception {
        // Arrange - use reflection to access private method
        java.lang.reflect.Method generateFileNameMethod = FileStorageService.class
                .getDeclaredMethod("generateFileName", String.class);
        generateFileNameMethod.setAccessible(true);

        // Act
        String name = (String) generateFileNameMethod.invoke(fileStorageService, (String) null);

        // Assert
        assertTrue(name.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"),
                "Name should contain only UUID without extension");
    }
}
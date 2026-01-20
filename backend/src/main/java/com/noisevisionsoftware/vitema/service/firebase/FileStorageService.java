package com.noisevisionsoftware.vitema.service.firebase;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final Storage storage;

    @Value("${firebase.storage.bucket-name}")
    private String bucketName;

    public String uploadFile(MultipartFile file, String userId) throws IOException {
        try {
            String fileName = generateFileName(file.getOriginalFilename());
            String filePath = String.format("diets/%s/%s", userId, fileName);

            if (!storage.get(bucketName).exists()) {
                throw new IOException("Storage bucket does not exist: " + bucketName);
            }

            BlobId blobId = BlobId.of(bucketName, filePath);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();

            Blob blob = storage.create(blobInfo, file.getBytes());

            return blob.signUrl(365, TimeUnit.DAYS).toString();
        } catch (Exception e) {
            log.error("Failed to upload file to Firebase Storage: {}", e.getMessage());
            throw e;
        }
    }

    private String generateFileName(String originalFileName) {
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        return UUID.randomUUID() + extension;
    }
}

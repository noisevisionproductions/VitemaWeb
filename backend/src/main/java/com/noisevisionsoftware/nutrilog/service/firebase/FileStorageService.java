package com.noisevisionsoftware.nutrilog.service.firebase;

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
            log.info("Uploading file to Firebase Storage: {}", filePath);

            if (!storage.get(bucketName).exists()) {
                log.error("Bucket {} does not exist", bucketName);
                throw new IOException("Storage bucket does not exist: " + bucketName);
            }

            BlobId blobId = BlobId.of(bucketName, filePath);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();

            Blob blob = storage.create(blobInfo, file.getBytes());

            // Generowanie publicznego URL z sygnaturą, który wygaśnie po 365 dniach
            String signedUrl = blob.signUrl(365, TimeUnit.DAYS).toString();
            log.info("File successfully uploaded. Signed URL: {}", signedUrl);

            return signedUrl;
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

package com.noisevisionsoftware.vitema.config.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
@Slf4j
public class FirebaseStorageConfig {

    @Value("${firebase.storage.bucket-name}")
    private String bucketName;

    @Value("${firebase.service-account.path}")
    private String serviceAccountPath;

    @Bean
    public Storage storage() throws IOException {
        try {
            InputStream serviceAccount;
            Resource resource = new ClassPathResource(serviceAccountPath);

            if (resource.exists()) {
                log.info("Loading Firebase Storage credentials from classpath: {}", serviceAccountPath);
                serviceAccount = resource.getInputStream();
            } else {
                File file = new File(serviceAccountPath);
                if (file.exists()) {
                    log.info("Loading Firebase Storage credentials from filesystem: {}", file.getAbsolutePath());
                    serviceAccount = new FileInputStream(file);
                } else {
                    throw new IOException("Firebase config file not found at: " + serviceAccountPath);
                }
            }

            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);

            return StorageOptions.newBuilder()
                    .setCredentials(credentials)
                    .setProjectId(extractProjectId(bucketName))
                    .build()
                    .getService();
        } catch (IOException e) {
            log.error("Failed to initialize Firebase Storage", e);
            throw e;
        }
    }

    private String extractProjectId(String bucketName) {
        if (bucketName == null) return "unknown-project";
        return bucketName.replace(".appspot.com", "");
    }
}
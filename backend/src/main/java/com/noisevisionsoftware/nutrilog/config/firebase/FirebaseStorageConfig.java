package com.noisevisionsoftware.nutrilog.config.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

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
            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(new ClassPathResource(serviceAccountPath).getInputStream());

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
        // Remove .appspot.com from bucket name to get project ID
        return bucketName.replace(".appspot.com", "");
    }
}
package com.noisevisionsoftware.nutrilog.config.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.*;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.service-account.path}")
    private String firebaseConfigPath;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            InputStream serviceAccount;

            try {
                Resource classpathResource = new ClassPathResource(firebaseConfigPath);
                if (classpathResource.exists()) {
                    serviceAccount = classpathResource.getInputStream();
                    log.info("Loading Firebase config from classpath");
                } else {
                    File file = new File(firebaseConfigPath);
                    if (file.exists()) {
                        serviceAccount = new FileInputStream(file);
                        log.info("Loading Firebase config from absolute path: {}", file.getAbsolutePath());
                    } else {
                        throw new FileNotFoundException("Firebase config file not found at: " + firebaseConfigPath);
                    }
                }

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                return FirebaseApp.initializeApp(options);
            } catch (Exception e) {
                log.error("Failed to initialize Firebase: {}", e.getMessage(), e);
                throw e;
            }
        }
        return FirebaseApp.getInstance();
    }

    @Bean
    public FirebaseAuth firebaseAuth(FirebaseApp firebaseApp) {
        return FirebaseAuth.getInstance(firebaseApp);
    }

    @Bean
    public Firestore firestore() throws IOException {
        return FirestoreClient.getFirestore(firebaseApp());
    }
}
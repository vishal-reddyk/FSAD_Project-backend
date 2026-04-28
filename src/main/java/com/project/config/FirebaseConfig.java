package com.project.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initFirebase() {
        try {
            String firebaseConfig = System.getenv("FIREBASE_KEY");

            if (firebaseConfig == null || firebaseConfig.isBlank()) {
                System.out.println("ℹ️ FIREBASE_KEY not set; skipping Firebase initialization");
                return;
            }

            InputStream serviceAccount =
                    new ByteArrayInputStream(firebaseConfig.getBytes());

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

            System.out.println("✅ Firebase initialized");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

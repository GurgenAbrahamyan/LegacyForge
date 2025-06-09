package com.gamb1t.server.DBcontrol;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.FileInputStream;

public class FirebaseInitializer {
    public static void initFirebase() {
        try {
            FileInputStream serviceAccount = new FileInputStream("legacy-forge-firebase-adminsdk-fbsvc-27917c46d1.json");
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://legacy-forge-default-rtdb.firebaseio.com/")
                .setProjectId("legacy-forge")

                .build();
            FirebaseApp.initializeApp(options);
            System.out.println("Firebase initialized successfully");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Firebase initialization failed");
        }
    }
}

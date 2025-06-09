package com.gamb1t.server.DBcontrol;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;
import com.gamb1t.legacyforge.Entity.User;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class FirebaseServiceServer {
    private static FirebaseServiceServer instance;

    private FirebaseServiceServer() {
        try {
            FileInputStream serviceAccount = new FileInputStream("legacy-forge-firebase-adminsdk-fbsvc-27917c46d1.json");
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://legacy-forge-default-rtdb.firebaseio.com/")
                .setProjectId("legacy-forge")
                .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase initialized successfully");
            } else {
                System.out.println("Firebase already initialized");
            }
        } catch (IOException e) {
            System.err.println("Failed to initialize Firebase: " + e.getMessage());
        }
    }

    public static synchronized FirebaseServiceServer getInstance() {
        if (instance == null) {
            instance = new FirebaseServiceServer();
        }
        return instance;
    }

    public CompletableFuture<Void> updateUserStats(String firebaseId, int wins, int losses, int rating) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        DatabaseReference userRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(firebaseId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("wins", wins);
        updates.put("losses", losses);
        updates.put("rating", rating);

        userRef.updateChildren(updates, (error, ref) -> {
            if (error != null) {
                System.err.println("Failed to update user stats for " + firebaseId + ": " + error.getMessage());
                future.completeExceptionally(error.toException());
            } else {
                System.out.println("Updated stats for user " + firebaseId + ": wins=" + wins + ", losses=" + losses + ", rating=" + rating);
                future.complete(null);
            }
        });

        return future;
    }

    public CompletableFuture<User> getUser(String firebaseId) {
        CompletableFuture<User> future = new CompletableFuture<>();
        DatabaseReference userRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(firebaseId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        future.complete(user);
                    } else {
                        future.completeExceptionally(new Exception("Failed to deserialize user data"));
                    }
                } else {
                    future.completeExceptionally(new Exception("User not found"));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Failed to fetch user " + firebaseId + ": " + error.getMessage());
                future.completeExceptionally(error.toException());
            }
        });

        return future;
    }

    public CompletableFuture<Void> updatePlayerStatsFromUser(User winner, User loser, int ratingChange) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        if (winner == null || winner.firebaseId == null || loser == null || loser.firebaseId == null) {
            future.completeExceptionally(new IllegalArgumentException("Invalid winner or loser User object"));
            return future;
        }

        // Prepare updates for both winner and loser
        Map<String, Object> winnerUpdates = new HashMap<>();
        winnerUpdates.put("wins", winner.wins + 1);
        winnerUpdates.put("rating", winner.rating + ratingChange);

        Map<String, Object> loserUpdates = new HashMap<>();
        loserUpdates.put("losses", loser.losses + 1);
        loserUpdates.put("rating", Math.max(0, loser.rating - ratingChange)); // Prevent negative rating

        DatabaseReference winnerRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(winner.firebaseId);
        DatabaseReference loserRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(loser.firebaseId);

        CompletableFuture<Void> winnerFuture = new CompletableFuture<>();
        CompletableFuture<Void> loserFuture = new CompletableFuture<>();

        winnerRef.updateChildren(winnerUpdates, (error, ref) -> {
            if (error != null) {
                System.err.println("Failed to update winner " + winner.firebaseId + ": " + error.getMessage());
                winnerFuture.completeExceptionally(error.toException());
            } else {
                System.out.println("Updated winner " + winner.firebaseId + ": wins=" + (winner.wins + 1) + ", rating=" + (winner.rating + ratingChange));
                winnerFuture.complete(null);
            }
        });

        loserRef.updateChildren(loserUpdates, (error, ref) -> {
            if (error != null) {
                System.err.println("Failed to update loser " + loser.firebaseId + ": " + error.getMessage());
                loserFuture.completeExceptionally(error.toException());
            } else {
                System.out.println("Updated loser " + loser.firebaseId + ": losses=" + (loser.losses + 1) + ", rating=" + Math.max(0, loser.rating - ratingChange));
                loserFuture.complete(null);
            }
        });

        CompletableFuture.allOf(winnerFuture, loserFuture)
            .whenComplete((result, error) -> {
                if (error != null) {
                    future.completeExceptionally(error);
                } else {
                    future.complete(null);
                }
            });

        return future;
    }
}

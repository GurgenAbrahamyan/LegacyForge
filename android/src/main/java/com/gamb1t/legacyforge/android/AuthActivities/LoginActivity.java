package com.gamb1t.legacyforge.android.AuthActivities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gamb1t.legacyforge.Entity.User;
import com.gamb1t.legacyforge.R;
import com.gamb1t.legacyforge.android.GameModeChoosing;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailField, passwordField;
    private Button loginButton, goToRegisterButton, guestButton;

    private String tempAcc = "bopirep699@dlbazi.com";
    private String tempAccPass = "123456789";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        mAuth = FirebaseAuth.getInstance();

        emailField = findViewById(R.id.editTextEmail);
        passwordField = findViewById(R.id.editTextPassword);
        goToRegisterButton = findViewById(R.id.register_button);
        loginButton = findViewById(R.id.login);
        guestButton = findViewById(R.id.guest);

        loginButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(email, password);
            }
        });

        goToRegisterButton.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });

        loginButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(email, password);
            }
        });

        guestButton.setOnClickListener(v -> {
            String email = tempAcc;
            String password = tempAccPass;

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(email, password);
            }
        });

    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email.toLowerCase(), password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isEmailVerified()) {
                        String uid = mAuth.getCurrentUser().getUid();

                        DatabaseReference userRef = FirebaseDatabase.getInstance()
                            .getReference("users").child(uid);

                        userRef.get().addOnSuccessListener(snapshot -> {
                            if (snapshot.exists()) {
                                User user = snapshot.getValue(User.class);

                                if (user != null) {
                                    System.out.println(user.equippedWeapon);
                                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(this, GameModeChoosing.class);
                                    intent.putExtra("user", user);
                                    intent.putExtra("playerId", uid);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(this, "Error parsing user data.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(this, "User data not found in database.", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(e -> {
                            Toast.makeText(this, "Error fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        Toast.makeText(this, "Please verify your email address.", Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                    }
                } else {
                    Toast.makeText(this, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("LoginActivity", "Login failed", task.getException());
                }
            });
    }


}

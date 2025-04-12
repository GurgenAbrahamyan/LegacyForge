package com.gamb1t.legacyforge.android.AuthActivities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gamb1t.legacyforge.ManagerClasses.PlayerData;
import com.gamb1t.legacyforge.R;
import com.gamb1t.legacyforge.android.GameLauncher;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailField, passwordField;
    private Button loginButton, goToRegisterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        mAuth = FirebaseAuth.getInstance();

        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        goToRegisterButton = findViewById(R.id.register_button);

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
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isEmailVerified()) {
                        String uid = mAuth.getCurrentUser().getUid();

                        DatabaseReference userRef = FirebaseDatabase.getInstance()
                            .getReference("users").child(uid);

                        userRef.get().addOnSuccessListener(snapshot -> {
                            if (snapshot.exists()) {

                                String nickname = snapshot.child("nickname").getValue(String.class);
                                int level = snapshot.child("level").getValue(Integer.class);
                                int exp = snapshot.child("experience").getValue(Integer.class);
                                int money = snapshot.child("money").getValue(Integer.class);


                                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(this, GameLauncher.class);
                                intent.putExtra("nickname", nickname);
                                System.out.println(money);
                                intent.putExtra("money", money);

                                intent.putExtra("playerId", mAuth.getCurrentUser().getUid());
                                startActivity(intent);
                                finish();
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

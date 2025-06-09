package com.gamb1t.legacyforge.android.AuthActivities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.gamb1t.legacyforge.Entity.Items;
import com.gamb1t.legacyforge.Entity.User;
import com.gamb1t.legacyforge.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailField, passwordField, nicknameField;
    private Button registerButton, backButton;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        emailField = findViewById(R.id.editTextEmail);
        passwordField = findViewById(R.id.editTextPassword);
        nicknameField = findViewById(R.id.editTextName);
        registerButton = findViewById(R.id.register_button);
        backButton = findViewById(R.id.back_button);

        registerButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();
            String nickname = nicknameField.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || nickname.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            if(nickname.length() > 14){
                Toast.makeText(this, "Nickname should not be longer than 14 characters.", Toast.LENGTH_SHORT).show();
                return;
            }

            checkNicknameExists(nickname, () -> registerUser(email, password, nickname));
        });

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void checkNicknameExists(String nickname, Runnable onAvailable) {
        usersRef.orderByChild("nickname").equalTo(nickname)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(RegisterActivity.this, "Nickname is already taken!", Toast.LENGTH_SHORT).show();
                    } else {
                        onAvailable.run();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(RegisterActivity.this, "Error checking nickname: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void registerUser(String email, String password, String nickname) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        String uid = user.getUid();

                        User newUser = new User(nickname, 1, 0, 0, null, null, null, new Items(new HashMap<>(), new HashMap<>()), uid);
                        DatabaseReference userRef = usersRef.child(uid);

                        userRef.setValue(newUser).addOnSuccessListener(aVoid -> {
                            DatabaseReference weaponRef = userRef.child("items").child("weapons").push();
                            String weaponId = weaponRef.getKey();

                            HashMap<String, Object> weaponData = new HashMap<>();
                            weaponData.put("name", "Iron Sword");
                            weaponData.put("level", 1);

                            weaponRef.setValue(weaponData).addOnSuccessListener(v -> {
                                userRef.child("equippedWeapon").setValue(weaponId);


                                DatabaseReference armorRef1 = userRef.child("items").child("armor").push();
                                String armorId1 = armorRef1.getKey();
                                HashMap<String, Object> armorData1 = new HashMap<>();
                                armorData1.put("name", "Steel Chestplate");
                                armorData1.put("level", 1);

                                userRef.child("equippedArmorChestPlate").setValue(armorId1);

                                DatabaseReference armorRef2 = userRef.child("items").child("armor").push();
                                String armorId2 = armorRef2.getKey();
                                HashMap<String, Object> armorData2 = new HashMap<>();
                                armorData2.put("name", "Iron Helmet");
                                armorData2.put("level", 1);

                                userRef.child("equippedArmorHelmet").setValue(armorId2);


                                armorRef1.setValue(armorData1);
                                armorRef2.setValue(armorData2).addOnSuccessListener(v2 -> {
                                    user.sendEmailVerification();
                                    Toast.makeText(this, "Registered! Check email for verification!", Toast.LENGTH_LONG).show();

                                    Intent gameIntent = new Intent(this, LoginActivity.class);
                                    gameIntent.putExtra("nickname", nickname);
                                    gameIntent.putExtra("money", 0);
                                    gameIntent.putExtra("exp", 0);
                                    gameIntent.putExtra("lvl", 1);
                                    gameIntent.putExtra("playerId", uid);
                                    startActivity(gameIntent);
                                    finish();
                                }).addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to create starter armors: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            }).addOnFailureListener(e ->
                                Toast.makeText(this, "Failed to create starter weapon: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }).addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                } else {
                    Toast.makeText(this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("RegisterActivity", "Registration error", task.getException());
                }
            });
    }
}

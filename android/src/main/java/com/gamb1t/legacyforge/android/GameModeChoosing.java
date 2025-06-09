package com.gamb1t.legacyforge.android;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gamb1t.legacyforge.R;
import com.gamb1t.legacyforge.Entity.User;
import com.gamb1t.legacyforge.android.AuthActivities.LoginActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GameModeChoosing extends AppCompatActivity {

    private Button singlePlayerButton;
    private Button multiPlayerButton;
    private Button logOut;

    private User user;
    private String uId;
    private String serverIp;

    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_mode_choosing);

        singlePlayerButton = findViewById(R.id.single_player_button);
        multiPlayerButton = findViewById(R.id.multi_player_button);
        logOut = findViewById(R.id.log_out_button);

        user = (User) getIntent().getSerializableExtra("user");
        uId = getIntent().getStringExtra("playerId");

        if (user == null || uId == null) {
            finish();
            return;
        }

        singlePlayerButton.setOnClickListener(v -> {
            Intent intent = new Intent(GameModeChoosing.this, SinglePlayerActivity.class);
            intent.putExtra("user", user);
            intent.putExtra("playerId", uId);
            startActivity(intent);
        });

        multiPlayerButton.setOnClickListener(v -> {
            multiPlayerButton.setEnabled(false);
            fetchServerIp();
        });

        logOut.setOnClickListener(v -> {
            Intent intent = new Intent(GameModeChoosing.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void fetchServerIp() {
        database.child("serverIp").get().addOnCompleteListener(task -> {
            multiPlayerButton.setEnabled(true);

            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                serverIp = snapshot.getValue(String.class);
                if (serverIp != null) {
                    System.out.println("Retrieved Server IP: " + serverIp);
                } else {
                    System.err.println("No server IP found in Firebase");
                    serverIp = "127.0.0.1";
                }
            } else {
                System.err.println("Failed to fetch server IP: " + task.getException().getMessage());
                serverIp = "127.0.0.1";
            }


            Intent intent = new Intent(GameModeChoosing.this, MultiPlayerActivity.class);
            intent.putExtra("user", user);
            intent.putExtra("playerId", uId);
            intent.putExtra("serverIp", serverIp);
            startActivity(intent);
        });
    }
}

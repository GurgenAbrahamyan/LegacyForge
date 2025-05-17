package com.gamb1t.legacyforge.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gamb1t.legacyforge.Main;
import com.gamb1t.legacyforge.R;

public class GameModeChoosing extends AppCompatActivity {

    private Button singlePlayerButton;
    private Button multiPlayerButton;

    String nickname;
    int level;
    int experience;
    int money;

    String uID;
    Main main;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_mode_choosing);

        singlePlayerButton = findViewById(R.id.single_player_button);
        multiPlayerButton = findViewById(R.id.multi_player_button);

        singlePlayerButton.setOnClickListener(v -> {
            Intent intent = new Intent(GameModeChoosing.this, SinglePlayerActivity.class);
            nickname = getIntent().getStringExtra("nickname");
            intent.putExtra("nickname", nickname);
            money = getIntent().getIntExtra("money", 0);
            intent.putExtra("money", money);
            System.out.println(money);
            uID =getIntent().getStringExtra("playerId");
            intent.putExtra("playerId", uID);

            startActivity(intent);
        });

        multiPlayerButton.setOnClickListener(v -> {

            Intent intent = new Intent(GameModeChoosing.this, MultiPlayerActivity.class);
            nickname = getIntent().getStringExtra("nickname");
            intent.putExtra("nickname", nickname);
            money = getIntent().getIntExtra("money", 0);
            intent.putExtra("money", money);
            System.out.println(money);
            uID =getIntent().getStringExtra("playerId");
            intent.putExtra("playerId", uID);

           startActivity(intent);
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}

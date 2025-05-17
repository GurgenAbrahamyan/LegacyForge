package com.gamb1t.legacyforge.android;


import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.esotericsoftware.kryonet.Client;

import com.gamb1t.clientside.ClientMain;
import com.google.firebase.FirebaseApp;

public class MultiPlayerActivity extends AndroidApplication {

    String nickname;
    int level;
    int experience;
    int money;

    String uID;

    ClientMain main;


    private ServerSync serverSync;

    private final Handler syncHandler = new Handler();
    private final int SYNC_INTERVAL = 5000;
    private int lastSyncedMoney = -1;

   private final Runnable moneySyncRunnable = new Runnable() {

       @Override
        public void run() {
          if (main != null && serverSync != null && main.isInitialized()) {
                int currentMoney = main.getMoney();

                if (currentMoney != lastSyncedMoney) {
                    serverSync.updateMoney(currentMoney);
                    lastSyncedMoney = currentMoney;
                }
            }
            syncHandler.postDelayed(this, SYNC_INTERVAL);
        }


    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nickname = getIntent().getStringExtra("nickname");
        money = getIntent().getIntExtra("money", 0);
        System.out.println(money);
        uID =getIntent().getStringExtra("playerId");

        serverSync = new ServerSync(uID);

      main = new ClientMain(nickname, experience, level, 100, money);

      FirebaseApp.initializeApp(this);

        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        startGame();

        syncHandler.post(moneySyncRunnable);
    }

    public void startGame() {
        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = true;
        initialize(main, configuration);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       // syncHandler.removeCallbacks(moneySyncRunnable); // stop syncing
    }
}

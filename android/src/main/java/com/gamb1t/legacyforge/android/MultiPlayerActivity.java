package com.gamb1t.legacyforge.android;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.gamb1t.legacyforge.Entity.User;
import com.gamb1t.legacyforge.Networking.PlayerChangeListener;
import com.gamb1t.legacyforge.Weapons.Armor;
import com.gamb1t.legacyforge.Weapons.Weapon;
import com.gamb1t.clientside.ClientMain;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.*;
import java.util.HashMap;
import java.util.Map;

public class MultiPlayerActivity extends AndroidApplication {
    private User user;
    private String uId;
    private ClientMain main;
    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    private String serverIp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = (User) getIntent().getSerializableExtra("user");
        uId = getIntent().getStringExtra("playerId");

        FirebaseApp.initializeApp(this);

        serverIp = getIntent().getStringExtra("serverIp");
        System.out.println("Server IP: " + serverIp);

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
    }

    private void startGame() {
        PlayerChangeListener playerChangeListener = new PlayerChangeListener() {
            @Override
            public void onPlayerExpAndMoneyChange(int money, float experience) {
                database.child("users").child(uId).child("money").setValue(money);
                database.child("users").child(uId).child("experience").setValue(experience);
            }

            @Override
            public void onPlayerLevelChange(int lvl) {
                database.child("users").child(uId).child("level").setValue(lvl);
            }

            @Override
            public void onPlayerNewInventoryAdd(Object object) {
                DatabaseReference itemRef;
                if (object instanceof Weapon) {
                    Weapon weapon = (Weapon) object;
                    itemRef = database.child("users").child(uId).child("items").child("weapons").push();
                    Map<String, Object> itemData = new HashMap<>();
                    itemData.put("name", weapon.getName());
                    itemData.put("level", weapon.getLevel());
                    itemRef.setValue(itemData).addOnSuccessListener(aVoid -> {
                        weapon.setFirebaseId(itemRef.getKey());
                    });
                } else if (object instanceof Armor) {
                    DatabaseReference usersRef = database.child("users");
                    DatabaseReference userRef = usersRef.child(uId);
                    DatabaseReference armorRef = userRef.child("items").child("armor").push();
                    String armorId = armorRef.getKey();
                    ((Armor) object).setFirebaseId(armorId);
                    HashMap<String, Object> armorData = new HashMap<>();
                    armorData.put("name", ((Armor) object).getName());
                    armorData.put("level", ((Armor) object).getLevel());
                    armorRef.setValue(armorData).addOnSuccessListener(v -> {
                        if (((Armor) object).getType().equals("helmet")) {
                            userRef.child("equippedArmorHelmet").setValue(armorId);
                        } else {
                            userRef.child("equippedArmorChestPlate").setValue(armorId);
                        }
                    });
                }
            }

            @Override
            public void removeItemById(Object object) {
                String type = (object instanceof Weapon) ? "weapons" : "armor";
                String id = null;
                if (object instanceof Weapon) {
                    id = ((Weapon) object).getFireBaseId();
                } else if (object instanceof Armor) {
                    id = ((Armor) object).getFirebaseId();
                }
                if (id != null) {
                    database.child("users").child(uId).child("items").child(type).child(id).removeValue();
                }
            }

            @Override
            public void onPlayerEquip(Object o) {
                if (o instanceof Weapon) {
                    Weapon w = (Weapon) o;
                    String id = w.getFireBaseId();
                    if (id != null) {
                        database.child("users").child(uId).child("equippedWeapon").setValue(id);
                    }
                } else if (o instanceof Armor) {
                    Armor a = (Armor) o;
                    String id = a.getFirebaseId();
                    if (id != null) {
                        String field = a.getType().equalsIgnoreCase("helmet") ? "equippedArmorHelmet" : "equippedArmorChestPlate";
                        database.child("users").child(uId).child(field).setValue(id);
                    }
                }
            }

            @Override
            public void onReturnToGameModeSelection(boolean intended) {
                runOnUiThread(() -> {
                    if (main != null && !intended) {
                        Toast.makeText(MultiPlayerActivity.this, "Connection to the server failed. Please try again.", Toast.LENGTH_LONG).show();
                    }
                    if (main != null) {
                        main.disconnect();
                        if(main.isIntentionalDisconnect()){
                        user = main.convertPlayerToUser(main.gameScreen.getPLAYER());
                            System.out.println("converted");
                        }
                    }
                    Intent intent = new Intent(MultiPlayerActivity.this, GameModeChoosing.class);
                    intent.putExtra("user", user);
                    intent.putExtra("playerId", uId);
                    startActivity(intent);
                    finish();
                });
            }
        };

        main = new ClientMain(user, playerChangeListener, serverIp);
        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = true;
        initialize(main, configuration);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (main != null) {
            main.disconnect();
            main = null;
        }
    }
}

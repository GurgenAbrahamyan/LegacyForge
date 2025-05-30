package com.gamb1t.legacyforge.android;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.gamb1t.legacyforge.Main;
import com.gamb1t.legacyforge.Networking.PlayerChangeListener;
import com.gamb1t.legacyforge.Weapons.Armor;
import com.gamb1t.legacyforge.Weapons.Weapon;
import com.gamb1t.legacyforge.Entity.User;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SinglePlayerActivity extends AndroidApplication {

    private User player;
    private String uId;
    private Main main;
    private PlayerChangeListener firebasePlayerListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        player = (User) getIntent().getSerializableExtra("user");
        uId = getIntent().getStringExtra("playerId");

        FirebaseApp.initializeApp(this);

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        firebasePlayerListener = new PlayerChangeListener() {
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


                     DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                    DatabaseReference userRef = usersRef.child(uId);
                    DatabaseReference armorRef = userRef.child("items").child("armor").push();
                    String armorId = armorRef.getKey();

                    ((Armor) object).setFirebaseId(armorId);
                    HashMap<String, Object> weaponData = new HashMap<>();
                    weaponData.put("name", ((Armor) object).getName());
                    weaponData.put("level", ((Armor) object).getLevel());

                    armorRef.setValue(weaponData).addOnSuccessListener(v -> {
                        if(((Armor) object).getType().equals("helmet")){
                            userRef.child("equippedArmorHelmet").setValue(armorId);

                        }
                        else {
                            userRef.child("equippedArmorChestPlate").setValue(armorId);

                        }
                    }
                        );

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
                    System.out.println(w.getFireBaseId());
                    String id = w.getFireBaseId();
                    if (id != null) {
                        database.child("users").child(uId).child("equippedWeapon").setValue(id);
                    }
                } else if (o instanceof Armor) {
                    Armor a = (Armor) o;
                    String id = a.getFirebaseId();
                    if (id != null) {
                        String field = a.getType().equalsIgnoreCase("helmet")? "equippedArmorHelmet" : "equippedArmorChestPlate";
                        database.child("users").child(uId).child(field).setValue(id);
                    }
                }

            }
        };

        main = new Main(player, firebasePlayerListener);

        enableImmersiveMode();
        startGame();
    }

    private void enableImmersiveMode() {
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void startGame() {
        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = true;
        initialize(main, configuration);
    }
}

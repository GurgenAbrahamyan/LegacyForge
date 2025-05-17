package com.gamb1t.legacyforge.android;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class ServerSync {
    private String playerId;
    private DatabaseReference database;

    public ServerSync(String playerId) {
        this.playerId = playerId;
        this.database = FirebaseDatabase.getInstance().getReference();
    }

    public void updateMoney(int money) {
        database.child("users").child(playerId).child("money").setValue(money);
    }
}

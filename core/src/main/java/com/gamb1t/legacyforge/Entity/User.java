    package com.gamb1t.legacyforge.Entity;

    import java.io.Serializable;

    public class User implements Serializable {
        public String nickname;
        public int level;
        public int experience;
        public int money;
        public int wins;
        public int losses;
        public int rating;


        public String equippedWeapon;
        public String equippedArmorHelmet;
        public String equippedArmorChestPlate;
        public String firebaseId;

        public Items items;

        public User() {}

        public User(String nickname, int level, int experience, int money, String equippedWeapon, String equippedArmorHelmet, String equippedArmorChestPlate, Items items, String id) {

            this.nickname = nickname;
            this.level = level;
            this.experience = experience;
            this.money = money;

            this.equippedWeapon = equippedWeapon;
            this.equippedArmorHelmet = equippedArmorHelmet;
            this.equippedArmorChestPlate = equippedArmorChestPlate;
            this.items= items;
            this.firebaseId = id;

            wins = 0;
            losses = 0;
            rating = 100;
        }
    }

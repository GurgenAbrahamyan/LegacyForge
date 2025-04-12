package com.gamb1t.legacyforge.android.AuthActivities;

public class User {
    public String nickname;
    public int level;
    public int experience;
    public int money;

    public User() {}

    public User(String nickname, int level, int experience, int money) {
        this.nickname = nickname;
        this.level = level;
        this.experience = experience;
        this.money = money;
    }
}

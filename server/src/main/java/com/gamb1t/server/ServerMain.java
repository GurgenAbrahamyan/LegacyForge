package com.gamb1t.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.gamb1t.server.DBcontrol.FirebaseInitializer;

public class ServerMain {
    public static void main(String[] args)  {


        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        FirebaseInitializer.initFirebase();
        new HeadlessApplication(new ServerCycle(), config);

        System.out.println("Looking for resources in: " + Gdx.files.internal("weapons.json").file().getAbsolutePath());

    }


}

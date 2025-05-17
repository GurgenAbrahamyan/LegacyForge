package com.gamb1t.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;

public class ServerMain {
    public static void main(String[] args)  {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        new HeadlessApplication(new ServerCycle(), config);

        System.out.println("Looking for resources in: " + Gdx.files.internal("weapons.json").file().getAbsolutePath());

    }


}

package com.gamb1t.legacyforge.ManagerClasses;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GameUI {
    private Stage stage;
    private Label nicknameLabel, levelLabel;
    private ProgressBar hpBar;
    private TextButton menuButton;
    private Table uiTable;
    private Table pauseMenu;
    private boolean isPaused = false;

    public GameUI() {
        stage = new Stage(new ScreenViewport());

        nicknameLabel = new Label("Nickname: ???", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        levelLabel = new Label("Level:", new Label.LabelStyle(new BitmapFont(), Color.WHITE));

        hpBar = new ProgressBar(0, 100, 1, false, new Skin(Gdx.files.internal("ui/uiskin.json")));
        hpBar.setValue(100);

        menuButton = new TextButton("☰", new Skin(Gdx.files.internal("ui/uiskin.json")));

        uiTable = new Table();
        uiTable.top().left();
        uiTable.setFillParent(true);
        uiTable.add(nicknameLabel).pad(10);
        uiTable.row();
        uiTable.add(levelLabel).pad(10);
        uiTable.row();
        uiTable.add(hpBar).width(200).pad(10);

        pauseMenu = new Table();
        pauseMenu.setVisible(false);
        pauseMenu.center();
        pauseMenu.setFillParent(true);
        pauseMenu.add(new Label("Paused", new Label.LabelStyle(new BitmapFont(), Color.WHITE))).pad(10);
        pauseMenu.row();

        TextButton resumeButton = new TextButton("Resume", new Skin(Gdx.files.internal("ui/uiskin.json")));
        pauseMenu.add(resumeButton).pad(10);

        menuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                isPaused = !isPaused;
                pauseMenu.setVisible(isPaused);
            }
        });

        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                isPaused = false;
                pauseMenu.setVisible(false);
            }
        });

        stage.addActor(uiTable);
        stage.addActor(menuButton);
        stage.addActor(pauseMenu);
    }

    public void update(String nickname, int level, float hp) {
        nicknameLabel.setText("Nickname: " + nickname);
        levelLabel.setText("Level: " + level);
        hpBar.setValue(hp);
    }

    public void render() {
        stage.act();
        stage.draw();
    }

    public Stage getStage() {
        return stage;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void dispose() {
        stage.dispose();
    }
}

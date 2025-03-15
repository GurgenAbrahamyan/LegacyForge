package com.gamb1t.legacyforge.android;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.Gdx;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.gamb1t.legacyforge.Main;
import com.gamb1t.legacyforge.AuthenticationListener;

public class FirebaseLoginScreen implements Screen {
    private Stage stage;
    private Skin skin;
    private TextField emailField, passwordField;
    private Button loginButton;
    private FirebaseAuth mAuth;
    private AuthenticationListener authenticationListener;

    public FirebaseLoginScreen(Main game, AuthenticationListener authenticationListener) {
        this.authenticationListener = authenticationListener;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        mAuth = FirebaseAuth.getInstance();

        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        BitmapFont font = new BitmapFont();

        emailField = new TextField("", skin);
        passwordField = new TextField("", skin);
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('*');

        loginButton = new Button(skin);

        loginButton.addListener(event -> {
            if (event.toString().equals("touchUp")) {
                login();
                return true;
            }
            return false;
        });

        Table table = new Table();
        table.top().left();
        table.setFillParent(true);
        table.add(new Label("Email:", skin)).padBottom(10);
        table.row().padBottom(10);
        table.add(emailField).width(200);
        table.row().padBottom(10);
        table.add(new Label("Password:", skin)).padBottom(10);
        table.row().padBottom(10);
        table.add(passwordField).width(200);
        table.row().padBottom(20);
        table.add(loginButton).width(200).padBottom(20);

        stage.addActor(table);
    }

    private void login() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) return;

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    // Notify the listener about successful login
                    authenticationListener.onLoginSuccess();
                } else {
                    // Notify the listener about failure
                    authenticationListener.onLoginFailure("Login failed: " + task.getException().getMessage());
                }
            });
    }

    @Override
    public void render(float delta) {
        SpriteBatch batch = new SpriteBatch();
        batch.begin();
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void hide() {
        stage.dispose();
    }

    @Override
    public void dispose() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}
}

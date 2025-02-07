package com.gamb1t.legacyforge.ManagerClasses;

import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.GET_HEIGHT;
import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.GET_WIDTH;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class TouchManager implements InputProcessor {
    private GameScreen gameScreen;
    private float xCenter, yCenter, radius;
    private float xTouch, yTouch;
    private float bjoystickSize, sjoysticksize;
    private boolean touchDown;
    private final Texture joystickTexture;
    private final Texture smallerJoystickTexture;
    private final Sprite joystickSprite;
    private final Sprite smallerJoystickSprite;

    public TouchManager(GameScreen gameScreen) {
        this.gameScreen = gameScreen;


        bjoystickSize = GameConstants.Sprite.SIZE * 3;
        sjoysticksize = GameConstants.Sprite.SIZE * 2;


        xCenter = GET_WIDTH - GET_WIDTH / 5f;
        yCenter = GET_HEIGHT / 4f;

        joystickTexture = new Texture("joystick.png");
        joystickSprite = new Sprite(joystickTexture);
        joystickSprite.setSize((float) GET_WIDTH / 3, (float) GET_WIDTH / 3);
        joystickSprite.setPosition(xCenter - joystickSprite.getWidth() / 2, yCenter - joystickSprite.getHeight() / 2);

        smallerJoystickTexture = new Texture("joystick.png");
        smallerJoystickSprite = new Sprite(smallerJoystickTexture);
        smallerJoystickSprite.setSize(GET_WIDTH / 3f, GET_WIDTH / 3f);
        smallerJoystickSprite.setPosition(xCenter - smallerJoystickSprite.getWidth() / 2, yCenter - smallerJoystickSprite.getHeight() / 2);


        radius = bjoystickSize / 2;

        System.out.println(GET_HEIGHT + " " + GET_WIDTH);
    }

    public void draw(SpriteBatch batch) {
        batch.begin();
        batch.draw(joystickSprite, xCenter - bjoystickSize/2, yCenter - bjoystickSize/2, GameConstants.Sprite.SIZE * 3, GameConstants.Sprite.SIZE * 3);

        if (touchDown) {
            batch.draw(smallerJoystickSprite, xTouch - sjoysticksize / 2, yTouch - sjoysticksize / 2,
                GameConstants.Sprite.SIZE * 2, GameConstants.Sprite.SIZE * 2);
        } else {
            batch.draw(smallerJoystickSprite, xCenter - sjoysticksize/2, yCenter-sjoysticksize/2, GameConstants.Sprite.SIZE * 2, GameConstants.Sprite.SIZE * 2);
        }
        batch.end();
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }



    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        touchDown = false;
        gameScreen.setPlayerMoveFalse();
        return true;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        float x = screenX;
        float y = GET_HEIGHT - screenY;

        float a = Math.abs(x - xCenter);
        float b = Math.abs(y - yCenter);
        float c = (float) Math.hypot(a, b);

        if (c <= radius) {
            touchDown = true;
            xTouch = x;
            yTouch = y;
        }
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (touchDown) {
            xTouch = screenX;
            yTouch = GET_HEIGHT - screenY;
            float xDiff = xTouch - xCenter;
            float yDiff = yTouch - yCenter;

            gameScreen.setPlayerMoveTrue(new GameScreen.PointF(xDiff, yDiff));
        }
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}

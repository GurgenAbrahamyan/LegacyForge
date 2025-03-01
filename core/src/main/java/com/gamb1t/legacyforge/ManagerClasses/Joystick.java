package com.gamb1t.legacyforge.ManagerClasses;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Joystick {
    private float xCenter, yCenter, radius;
    private float xTouch, yTouch;
    private float joystickSize;
    private boolean touchDown;
    private final Texture joystickTexture;
    private final Sprite joystickSprite;
    private final Sprite smallerJoystickSprite;

    public Joystick(float xCenter, float yCenter, float joystickSize) {
        this.xCenter = xCenter;
        this.yCenter = yCenter;
        this.joystickSize = joystickSize;
        this.radius = joystickSize / 2;

        joystickTexture = new Texture("joystick.png");
        joystickSprite = new Sprite(joystickTexture);
        joystickSprite.setSize(joystickSize, joystickSize);
        joystickSprite.setPosition(xCenter - radius, yCenter - radius);

        smallerJoystickSprite = new Sprite(joystickTexture);
        smallerJoystickSprite.setSize(joystickSize / 1.5f, joystickSize / 1.5f);
    }

    public void draw(SpriteBatch batch) {
        batch.draw(joystickSprite, xCenter - radius, yCenter - radius, joystickSize, joystickSize);

        if (touchDown) {
            batch.draw(smallerJoystickSprite, xTouch - joystickSize / 3, yTouch - joystickSize / 3, joystickSize/ 1.5f ,joystickSize/ 1.5f);
        } else {
            batch.draw(smallerJoystickSprite, xCenter - joystickSize / 3, yCenter - joystickSize / 3, joystickSize/ 1.5f, joystickSize/ 1.5f);
        }
    }

    public void touchDown(float x, float y) {
        if (isTouched(x, y)) {
            touchDown = true;
            xTouch = x;
            yTouch = y;
        }
    }


    public void touchDragged(float x, float y) {
        if (touchDown) {
            xTouch = x;
            yTouch = y;
        }
    }

    public boolean isTouched(float x, float y) {
        float a = Math.abs(x - xCenter);
        float b = Math.abs(y - yCenter);
        float distance = (float) Math.hypot(a, b);

        return distance <= radius * 1.2f;
    }

    public void touchUp() {
        touchDown = false;
    }

    public float getXDiff() {
        return xTouch - xCenter;
    }

    public float getYDiff() {
        return yTouch - yCenter;
    }

    public float getAngle(){
        float dx = xTouch - xCenter;
        float dy = yTouch - yCenter;


        float angle = (float) Math.atan2(dy, dx);

        return  angle;
    }



}

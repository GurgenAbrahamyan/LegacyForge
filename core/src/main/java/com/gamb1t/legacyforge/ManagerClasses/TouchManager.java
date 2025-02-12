package com.gamb1t.legacyforge.ManagerClasses;

import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.GET_HEIGHT;
import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.GET_WIDTH;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.Weapons.Weapon;

public class TouchManager implements InputProcessor {
    private Player player;
    private Joystick movementJoystick;
    private Joystick attackJoystick;
    private Weapon weapon;

    private boolean[] touchDowns = new boolean[2];
    private float[] xTouches = new float[2];
    private float[] yTouches = new float[2];



    public TouchManager(Player player, Weapon weapon) {
        this.player = player;
        this.weapon = weapon;
        movementJoystick = new Joystick(GET_WIDTH / 6f, GET_HEIGHT / 4f, GameConstants.Sprite.SIZE * 3);
        attackJoystick = new Joystick(GET_WIDTH - GET_WIDTH / 6f, GET_HEIGHT / 4f, GameConstants.Sprite.SIZE * 3);

    }

    public void draw(SpriteBatch batch) {
        batch.begin();
        movementJoystick.draw(batch);
        attackJoystick.draw(batch);
        batch.end();
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        float x = screenX;
        float y = GET_HEIGHT - screenY;

        if (x < GET_WIDTH / 2f && !touchDowns[0] && movementJoystick.isTouched(x, y)) {
            movementJoystick.touchDown(x, y);
            touchDowns[0] = true;
            xTouches[0] = x;
            yTouches[0] = y;
        } else if (x >= GET_WIDTH / 2f && !touchDowns[1] && attackJoystick.isTouched(x, y)) {
            attackJoystick.touchDown(x, y);
            touchDowns[1] = true;
            xTouches[1] = x;
            yTouches[1] = y;
        }

        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        float x = screenX;

        if (x < GET_WIDTH / 2f) {
            movementJoystick.touchUp();
            touchDowns[0] = false;
        } else {
            attackJoystick.touchUp();
            weapon.setAtacking(true);
            weapon.attack();
            touchDowns[1] = false;
        }

        player.setPlayerMoveFalse();
        return true;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        float x = screenX;
        float y = GET_HEIGHT - screenY;

        if (x < GET_WIDTH / 2f && touchDowns[0]) {
            movementJoystick.touchDragged(x, y);
            player.setPlayerMoveTrue(new GameScreen.PointF(movementJoystick.getXDiff(), movementJoystick.getYDiff()));
        } else if (x >= GET_WIDTH / 2f && touchDowns[1]) {
            attackJoystick.touchDragged(x, y);

        }

        return true;
    }
    public Joystick getAttackJoystick(){
        return attackJoystick;
    }
    public boolean isAttackPressed(){
        return touchDowns[1];
    }

    @Override
    public boolean keyDown(int keycode) { return false; }
    @Override
    public boolean keyUp(int keycode) { return false; }
    @Override
    public boolean keyTyped(char character) { return false; }
    @Override
    public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override
    public boolean scrolled(float amountX, float amountY) { return false; }
}

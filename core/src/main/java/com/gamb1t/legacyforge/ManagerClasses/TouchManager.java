package com.gamb1t.legacyforge.ManagerClasses;

import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.GET_HEIGHT;
import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.GET_WIDTH;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.Networking.InputSender;
import com.gamb1t.legacyforge.Weapons.MagicWeapon;
import com.gamb1t.legacyforge.Weapons.MeleeWeapon;
import com.gamb1t.legacyforge.Weapons.RangedWeapon;
import com.gamb1t.legacyforge.Weapons.Weapon;

public class TouchManager implements InputProcessor {
    private Player player;
    private Joystick movementJoystick;
    private Joystick attackJoystick;
    private Weapon weapon;
    private InputSender inputSender;


    private float attackDirectionX, attackDirectionY;
    private boolean isAiming = false;
    float angle;

    private boolean isSinglePlayer = false;

    private boolean[] touchDowns = new boolean[2];

    public TouchManager(Player player, Weapon weapon, InputSender inputSender) {
        this.player = player;
        this.weapon = weapon;
        this.inputSender= inputSender;
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
        if(!player.isDead()){
            float x = screenX;
            float y = GET_HEIGHT - screenY;

            if (x < GET_WIDTH / 2f && !touchDowns[0] && movementJoystick.isTouched(x, y)) {
                movementJoystick.touchDown(x, y);
                touchDowns[0] = true;

            } else if (x >= GET_WIDTH / 2f && !touchDowns[1] && attackJoystick.isTouched(x, y)) {
                if (attackJoystick.isTouched(x, y)) {

                    if(weapon instanceof RangedWeapon ){
                        weapon.setAttacking(true);

                        ((RangedWeapon) weapon).setAnimOver(true);
                        inputSender.attackStart(true);
                        setIsAiming(true);
                        weapon.setAiming(isAiming);
                    }


                    attackJoystick.touchDown(x, y);
                    touchDowns[1] = true;

                }
            }}

        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {

        if(!player.isDead()){

            if (screenX < GET_WIDTH / 2f) {
                movementJoystick.touchUp();
                touchDowns[0] = false;
                player.setPlayerMoveFalse();
                inputSender.stopMove();

            }

            else if(screenX > GET_WIDTH/2f) {
                attackJoystick.touchUp();
                touchDowns[1] = false;
                rotationCalc();
                weapon.setRotation(angle);


                if (isAiming) {




                    if(isSinglePlayer){

                        weapon.attack();}
                    else {
                        if(weapon instanceof MeleeWeapon){
                            weapon.attack();
                        }
                    }





                    weapon.setAttacking(true);
                    inputSender.sendAttack(angle);
                    if(weapon instanceof  RangedWeapon){
                        ((RangedWeapon) weapon).setAnimOver(true);
                    }
                    if(weapon instanceof MagicWeapon){
                        ((MagicWeapon) weapon).setAnimOver(true);
                    }

                    if(weapon instanceof RangedWeapon){
                        ((RangedWeapon) weapon).setIsCharging(false);
                    }
                }


                isAiming = false;
                weapon.setAiming(isAiming);


            }

        }



        return true;
    }


    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }
    public void rotationCalc(){
        float length = (float) Math.sqrt(attackDirectionX * attackDirectionX + attackDirectionY * attackDirectionY);
        if (length != 0) {
            attackDirectionX /= length;
            attackDirectionY /= length;
        }
        else  if (attackDirectionX == 0 && attackDirectionY == 0) {
            attackDirectionX = 1;
            attackDirectionY = 0;}


        angle = (float) Math.toDegrees(Math.atan2(attackDirectionY, attackDirectionX));
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if(weapon instanceof RangedWeapon){
            rotationCalc();
            weapon.setRotation(angle);}

        if(!player.isDead()){
            float x = screenX;
            float y = GET_HEIGHT - screenY;


            if (x < GET_WIDTH / 2f && touchDowns[0]) {
                movementJoystick.touchDragged(x, y);
                player.setPlayerMoveTrue(new Vector2(movementJoystick.getXDiff(), movementJoystick.getYDiff()));
                inputSender.sendMove(new Vector2(movementJoystick.getXDiff(), movementJoystick.getYDiff()));
            }

            else if (x > GET_WIDTH / 2f && touchDowns[1]){

                attackJoystick.touchDragged(x, y);

                isAiming = true;
                weapon.setAiming(isAiming);

                inputSender.sendAttackDraged(angle);



                float newX = attackJoystick.getXDiff();
                float newY = attackJoystick.getYDiff();

                if (newX != 0 || newY != 0) {
                    attackDirectionX = attackJoystick.getXDiff();
                    attackDirectionY = attackJoystick.getYDiff();
                }

            }


        }

        return true;
    }

    public void setIsAiming(boolean set) { isAiming = set;}
    public boolean getIsAiming(){
        return  isAiming;
    }

    public float getRotation(){
        return angle;
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

    public void setWeapon(Weapon wp){
        weapon = wp;
    }

    public void setISinglePlayer(boolean b){
        this.isSinglePlayer = b;
    }

    public boolean getIsMultiplayer() {
        return isSinglePlayer;
    }
}

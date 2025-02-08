package com.gamb1t.legacyforge.ManagerClasses;



import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.GET_HEIGHT;
import static com.gamb1t.legacyforge.ManagerClasses.GameConstants.GET_WIDTH;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gamb1t.legacyforge.Entity.Enemy;
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.Enviroments.MapManaging;


import java.util.Random;



public class GameScreen implements Screen {

    private float playerX = ( float) GET_WIDTH / 2, playerY = (float) GET_HEIGHT / 2;
    private float cameraX, cameraY;
    private boolean movePlayer;

    private PointF lastTouchDiff;
    private Random rand = new Random();
    private TouchManager touchEvents;
    private PointF skeletonPos;
    private SpriteBatch batch;


    Player PLAYER = new Player("player_sprites/player_spritesheet.png", 4, 7);
    Enemy SKELETON = new Enemy("enemies_spritesheet/skeleton_spritesheet.png", 4, 7);



    private long lastDirChange = System.currentTimeMillis();


    private MapManaging mapManager;

    public GameScreen() {
        touchEvents = new TouchManager(this);
        Gdx.input.setInputProcessor(touchEvents);
        mapManager = new MapManaging("1room.txt","Tiles/tileset_floor.png", 30, 30);

        skeletonPos = new PointF(rand.nextInt(GET_WIDTH), rand.nextInt(GET_HEIGHT));
        batch = new SpriteBatch();


    }

    static class PointF{
        float x, y;
        PointF(float x, float y){
            this.x = x;
            this.y = y;
        }
    }


    public void update(double delta) {
        updatePlayerMove(delta);
        mapManager.setCameraValues(cameraX, cameraY);


        if (System.currentTimeMillis() - lastDirChange >= 3000) {
            int randFaceDir = rand.nextInt(4);
            SKELETON.setFaceDir(randFaceDir);
            lastDirChange = System.currentTimeMillis();
        }

        switch (SKELETON.getFaceDir()) {
            case GameConstants.Face_Dir.DOWN:
                skeletonPos.y += delta * 300;
                if (skeletonPos.y >= GET_HEIGHT)
                    SKELETON.setFaceDir( GameConstants.Face_Dir.UP);
                break;

            case GameConstants.Face_Dir.UP:
                skeletonPos.y -= delta * 300;
                if (skeletonPos.y <= 0)
                    SKELETON.setFaceDir(GameConstants.Face_Dir.DOWN);
                break;

            case GameConstants.Face_Dir.RIGHT:
                skeletonPos.x += delta * 300;
                if (skeletonPos.x >= GET_WIDTH)
                    SKELETON.setFaceDir(GameConstants.Face_Dir.LEFT);
                break;

            case GameConstants.Face_Dir.LEFT:
                skeletonPos.x -= delta * 300;
                if (skeletonPos.x <= 0)
                    SKELETON.setFaceDir(GameConstants.Face_Dir.RIGHT);
                break;
        }

        if(movePlayer){
            PLAYER.updateAnimation();}
        SKELETON.updateAnimation();

    }

    private void updatePlayerMove(double delta) {
        if (!movePlayer)
            return;

        float baseSpeed = (float) (delta * 300);
        float ratio = Math.abs(lastTouchDiff.y) / Math.abs(lastTouchDiff.x);
        double angle = Math.atan(ratio);

        float xSpeed = (float) Math.cos(angle);
        float ySpeed = (float) Math.sin(angle);


        if (xSpeed > ySpeed) {
            if (lastTouchDiff.x > 0) PLAYER.setFaceDir(GameConstants.Face_Dir.RIGHT) ;
            else PLAYER.setFaceDir(GameConstants.Face_Dir.LEFT);
        } else {
            if (lastTouchDiff.y > 0) PLAYER.setFaceDir(GameConstants.Face_Dir.DOWN);
            else PLAYER.setFaceDir(GameConstants.Face_Dir.UP);
        }

        if (lastTouchDiff.x < 0)
            xSpeed *= -1;
        if (lastTouchDiff.y < 0)
            ySpeed *= -1;

        int pWidth = GameConstants.Sprite.SIZE;
        int pHeight = GameConstants.Sprite.SIZE;

        if (xSpeed <= 0)
            pWidth = 0;
        if (ySpeed <= 0)
            pHeight = 0;


        float deltaX = xSpeed * baseSpeed * -1;
        float deltaY = ySpeed * baseSpeed * -1;

        if (mapManager.canMoveHere(playerX + cameraX * -1 + deltaX * -1 + pWidth, playerY + cameraY * -1 + deltaY * -1 + pHeight)) {
            cameraX += deltaX;
            cameraY += deltaY;
        }


    }




    public void setPlayerMoveTrue(PointF lastTouchDiff) {
        movePlayer = true;
        this.lastTouchDiff = lastTouchDiff;
    }

    public void setPlayerMoveFalse() {
        movePlayer = false;
        PLAYER.resetAnimation();
    }

    private void resetAnimation() {
        PLAYER.resetAnimation();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);



        mapManager.draw(batch);

        batch.begin();
        batch.draw(PLAYER.getSprite(PLAYER.getAniIndex(), PLAYER.getFaceDir()), playerX, playerY, GameConstants.Sprite.SIZE, GameConstants.Sprite.SIZE);
        batch.draw(SKELETON.getSprite(SKELETON.getAniIndex(), SKELETON.getFaceDir()), skeletonPos.x + cameraX, skeletonPos.y + cameraY, GameConstants.Sprite.SIZE, GameConstants.Sprite.SIZE );
        batch.end();

        touchEvents.draw(batch);
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}



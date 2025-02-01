package com.gamb1t.legacyforge;
import static com.gamb1t.legacyforge.MainActivity.GET_HEIGHT;
import static com.gamb1t.legacyforge.MainActivity.GET_WIDTH;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.gamb1t.legacyforge.Entities.GameCharacters;
import com.gamb1t.legacyforge.HelperClasses.GameConstants;
import com.gamb1t.legacyforge.enviorment.MapManaging;
import com.gamb1t.legacyforge.inputs.TouchEvents;

import java.util.Random;

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {

    private Paint redPaint = new Paint();
    private SurfaceHolder holder;
    private float playerX = GET_WIDTH / 2, playerY = GET_HEIGHT / 2;
    private float cameraX, cameraY;
    private boolean movePlayer;
    private PointF lastTouchDiff;
    private Random rand = new Random();
    private GameLoop gameLoop;
    private TouchEvents touchEvents;
    private PointF skeletonPos;
    private int skeletonDir = GameConstants.Face_Dir.DOWN;
    private long lastDirChange = System.currentTimeMillis();
    private int playerAniIndexY, playerFaceDir = GameConstants.Face_Dir.RIGHT;
    private int skeletonAniIndexY = 0;
    private int aniTick, aniTick2;
    private int aniSpeed = 10;

    private MapManaging mapManager;

    public GamePanel(Context context) {
        super(context);
        holder = getHolder();
        holder.addCallback(this);
        redPaint.setColor(Color.RED);
        touchEvents = new TouchEvents(this);
        gameLoop = new GameLoop(this);
        mapManager = new MapManaging();
        skeletonPos = new PointF(rand.nextInt(GET_WIDTH), rand.nextInt(GET_HEIGHT));


    }

    public void render() {
        Canvas c = holder.lockCanvas();
        c.drawColor(Color.BLACK);

        mapManager.draw(c);

        touchEvents.draw(c);

        c.drawBitmap(GameCharacters.PLAYER.getSprite(playerAniIndexY, playerFaceDir), playerX, playerY, null);
        c.drawBitmap(GameCharacters.SKELETON.getSprite(skeletonAniIndexY, skeletonDir), skeletonPos.x + cameraX, skeletonPos.y + cameraY, null);

        holder.unlockCanvasAndPost(c);
    }

    public void update(double delta) {
        updatePlayerMove(delta);
        mapManager.setCameraValues(cameraX, cameraY);


        if (System.currentTimeMillis() - lastDirChange >= 3000) {
            skeletonDir = rand.nextInt(4);
            lastDirChange = System.currentTimeMillis();
        }

        switch (skeletonDir) {
            case GameConstants.Face_Dir.DOWN:
                skeletonPos.y += delta * 300;
                if (skeletonPos.y >= GET_HEIGHT)
                    skeletonDir = GameConstants.Face_Dir.UP;
                break;

            case GameConstants.Face_Dir.UP:
                skeletonPos.y -= delta * 300;
                if (skeletonPos.y <= 0)
                    skeletonDir = GameConstants.Face_Dir.DOWN;
                break;

            case GameConstants.Face_Dir.RIGHT:
                skeletonPos.x += delta * 300;
                if (skeletonPos.x >= GET_WIDTH)
                    skeletonDir = GameConstants.Face_Dir.LEFT;
                break;

            case GameConstants.Face_Dir.LEFT:
                skeletonPos.x -= delta * 300;
                if (skeletonPos.x <= 0)
                    skeletonDir = GameConstants.Face_Dir.RIGHT;
                break;
        }


        updatePlayerAnimation();
        updateEntity();
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
            if (lastTouchDiff.x > 0) playerFaceDir = GameConstants.Face_Dir.RIGHT;
            else playerFaceDir = GameConstants.Face_Dir.LEFT;
        } else {
            if (lastTouchDiff.y > 0) playerFaceDir = GameConstants.Face_Dir.DOWN;
            else playerFaceDir = GameConstants.Face_Dir.UP;
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

    private void updatePlayerAnimation() {
        if (!movePlayer)
            return;
        aniTick++;
        if (aniTick >= aniSpeed) {
            aniTick = 0;
            playerAniIndexY++;
            if (playerAniIndexY >= 4)
                playerAniIndexY = 0;
        }
    }
    private void updateEntity(){
        aniTick2++;
        if (aniTick2 >= aniSpeed) {
            aniTick2 = 0;
            skeletonAniIndexY++;
            if (skeletonAniIndexY >= 4)
                skeletonAniIndexY = 0;
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return touchEvents.touchEvent(event);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        gameLoop.startGameLoop();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

    }

    public void setPlayerMoveTrue(PointF lastTouchDiff) {
        movePlayer = true;
        this.lastTouchDiff = lastTouchDiff;
    }

    public void setPlayerMoveFalse() {
        movePlayer = false;
        resetAnimation();
    }

    private void resetAnimation() {
        aniTick = 0;
        playerAniIndexY = 0;
    }


}
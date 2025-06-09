package com.gamb1t.legacyforge.ManagerClasses;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.gamb1t.legacyforge.Main;

public class SinglePlayerUi {
    private boolean isDungeonPanelOpen = false;
    private boolean isJoiningDungeon = false;
    private float dungeonCountdown = -1f;
    private static final float DUNGEON_COUNTDOWN_DURATION = 10f;

    private BitmapFont font;
    private Sprite panelTexture;
    private Sprite dungeonButton;
    private Sprite dungeonLeave;
    private Sprite closeButton;
    private Sprite openPanelButton;

    private Main clientMain;

    private Rectangle dungeonButtonBounds;
    private Rectangle leaveDungeonButtonBounds;
    private Rectangle closePanelButtonBounds;
    private Rectangle openPanelButtonBounds;

    private float panelX, panelY, panelWidth, panelHeight;
    private float btnWidth = GameConstants.Sprite.SIZE * 4;
    private float btnHeight = GameConstants.Sprite.SIZE * 4 / 3;

    // Add GlyphLayout for text measurement
    private final GlyphLayout glyphLayout = new GlyphLayout();

    public SinglePlayerUi(Main clientMain) {
        this.clientMain = clientMain;
        initializeRenderingObjects();
    }

    private void initializeRenderingObjects() {
        panelTexture = new Sprite(new Texture("ui/play_window_open.png"));

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("gothicbyte.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = GameConstants.Sprite.SIZE / 2;
        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS;
        font = generator.generateFont(parameter);
        generator.dispose();

        font.getData().setScale(1.5f);
        font.setColor(Color.BLACK);

        panelX = (float) (GameConstants.GET_WIDTH / 2) - GameConstants.GET_HEIGHT / 2;
        panelY = 0;
        panelWidth = GameConstants.GET_HEIGHT;
        panelHeight = GameConstants.GET_HEIGHT;

        openPanelButton = new Sprite(new Texture("ui/play_btn.png"));
        dungeonButton = new Sprite(new Texture("ui/join_btn.png"));
        dungeonLeave = new Sprite(new Texture("ui/leave_btn.png"));
        closeButton = new Sprite(new Texture("ui/close_btn.png"));

        float buttonSpacing = 10;
        float panelCenterX = panelX + panelWidth / 2;
        float panelCenterY = panelY + panelHeight / 2;

        dungeonButtonBounds = new Rectangle(
            panelCenterX - btnWidth / 2,
            GameConstants.Sprite.SIZE + btnHeight,
            btnWidth,
            btnHeight
        );

        leaveDungeonButtonBounds = new Rectangle(
            panelCenterX - btnWidth / 2,
            GameConstants.Sprite.SIZE ,
            btnWidth,
            btnHeight
        );

        closePanelButtonBounds = new Rectangle(
            panelX + panelWidth - GameConstants.Sprite.SIZE * 2,
            panelY + panelHeight - GameConstants.Sprite.SIZE * 2,
            GameConstants.Sprite.SIZE * 2, GameConstants.Sprite.SIZE * 2
        );

        openPanelButtonBounds = new Rectangle(
            GameConstants.GET_WIDTH/2 - btnWidth/2,
            GameConstants.GET_HEIGHT - btnHeight - GameConstants.Sprite.SIZE,
            btnWidth,
            btnHeight
        );
    }

    public void update(float delta) {
        if (isJoiningDungeon && dungeonCountdown > 0) {
            dungeonCountdown -= delta;
            if (dungeonCountdown <= 0) {
                dungeonCountdown = 0;
                isJoiningDungeon = false;
                clientMain.startSinglePlayerDungeon();
            }
        }

        if (Gdx.input.justTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = Gdx.input.getY();
            handleTouchInput(touchX, touchY);
        }
    }

    public void handleTouchInput(float touchX, float touchY) {
        touchY = GameConstants.GET_HEIGHT - touchY;
        if (!isDungeonPanelOpen) {
            if (openPanelButtonBounds.contains(touchX, touchY)) {
                isDungeonPanelOpen = true;
                Gdx.app.log("SinglePlayerUi", "Opened dungeon panel");
            }
        } else {
            if (closePanelButtonBounds.contains(touchX, touchY)) {
                isDungeonPanelOpen = false;
                Gdx.app.log("SinglePlayerUi", "Closed dungeon panel");
            } else if (!isJoiningDungeon && dungeonButtonBounds.contains(touchX, touchY)) {
                isJoiningDungeon = true;
                dungeonCountdown = DUNGEON_COUNTDOWN_DURATION;
                Gdx.app.log("SinglePlayerUi", "Started dungeon join countdown");
            } else if (isJoiningDungeon && leaveDungeonButtonBounds.contains(touchX, touchY)) {
                isJoiningDungeon = false;
                dungeonCountdown = -1f;
                Gdx.app.log("SinglePlayerUi", "Cancelled dungeon join");
            }
        }
    }

    public void render(SpriteBatch batch) {
        batch.begin();
        if (!isDungeonPanelOpen) {
            openPanelButton.setBounds(
                openPanelButtonBounds.x,
                openPanelButtonBounds.y,
                openPanelButtonBounds.width,
                openPanelButtonBounds.height
            );
            openPanelButton.draw(batch);
        } else {
            panelTexture.setBounds(panelX, panelY, panelWidth, panelHeight);
            panelTexture.draw(batch);

            if (!isJoiningDungeon) {
                dungeonButton.setBounds(
                    dungeonButtonBounds.x,
                    dungeonButtonBounds.y,
                    dungeonButtonBounds.width,
                    dungeonButtonBounds.height
                );
                dungeonButton.draw(batch);
            } else {
                dungeonLeave.setBounds(
                    leaveDungeonButtonBounds.x,
                    leaveDungeonButtonBounds.y,
                    leaveDungeonButtonBounds.width,
                    leaveDungeonButtonBounds.height
                );
                dungeonLeave.draw(batch);
            }

            closeButton.setBounds(
                closePanelButtonBounds.x,
                closePanelButtonBounds.y,
                closePanelButtonBounds.width,
                closePanelButtonBounds.height
            );
            closeButton.draw(batch);

            if (isJoiningDungeon && dungeonCountdown > 0) {
                String countdownText = (int) Math.ceil(dungeonCountdown) + "s";
                glyphLayout.setText(font, countdownText);
                float textWidth = glyphLayout.width;
                float centerX = panelX + panelWidth / 2;
                float textX = centerX - textWidth / 2;
                float textY = panelY + panelHeight / 2;
                font.draw(batch, countdownText, textX, textY);
            }
        }
        batch.end();
    }

    public void dispose() {
        panelTexture.getTexture().dispose();
        openPanelButton.getTexture().dispose();
        dungeonButton.getTexture().dispose();
        dungeonLeave.getTexture().dispose();
        closeButton.getTexture().dispose();
        font.dispose();
    }

    public boolean isDungeonPanelOpen() {
        return isDungeonPanelOpen;
    }

    public boolean isJoiningDungeon() {
        return isJoiningDungeon;
    }

    public float getDungeonCountdown() {
        return dungeonCountdown;
    }
}

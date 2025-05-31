package com.gamb1t.legacyforge.ManagerClasses;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.gamb1t.legacyforge.Entity.GameCharacters;
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.Networking.Network;
import com.esotericsoftware.kryonet.Client;
import com.gamb1t.legacyforge.Weapons.Weapon;

import java.util.ArrayList;

public class MultiplayerUi {
    private boolean isShopOpen = false;
    private boolean isNearShop = false;
    private boolean isSquadPanelOpen = false;
    private boolean isInDungeonSquad = false;
    private float squadCountdown = -1;
    private ArrayList<String> squadMemberNames = new ArrayList<>();

    private float shopX, shopY;
    private float shopWidth, shopHeight;
    private Polygon shopHitbox;
    private Sprite shopSprite;
    private float scrollX = 0;
    private ArrayList<Weapon> weaponList;
    private Weapon selectedWeapon;
    private BitmapFont font;
    private Sprite panelTexture;
    private Player player;

    private Client client;

    private Sprite btnJoinSquad, btnLeaveSquad, btnClosePanel, openPanelBtn, openPanelSprite;

    private float panelTile;
    private float btnWidth = GameConstants.Sprite.SIZE * 4;
    private float btnHeight = GameConstants.Sprite.SIZE * 2;
    private float panelX, panelY, panelWidth, panelHeight;

    private Rectangle joinSquadButtonBounds;
    private Rectangle leaveSquadButtonBounds;
    private Rectangle closePanelButtonBounds;
    private Rectangle openPanelButtonBounds;

    public MultiplayerUi(Player player, Client client) {
        this.player = player;
        this.client = client;
        initializeRendeingObjects();
    }

    public void initializeRendeingObjects() {
        this.panelTexture = new Sprite(new Texture("ui/play_window_open.png"));
        this.font = new BitmapFont();
        this.font.getData().setScale(1.5f); // Increased font size

        panelY = 0;
        panelWidth = GameConstants.GET_HEIGHT;
        panelHeight = GameConstants.GET_HEIGHT;
        panelX = (float) (GameConstants.GET_WIDTH / 2) - panelWidth / 2;

        panelTile = panelWidth / 16f;

        openPanelBtn = new Sprite(new Texture("ui/play_btn.png"));
        openPanelSprite = new Sprite(new Texture("ui/play_window_open.png"));
        btnJoinSquad = new Sprite(new Texture("ui/join_btn.png"));
        btnLeaveSquad = new Sprite(new Texture("ui/leave_btn.png"));
        btnClosePanel = new Sprite(new Texture("ui/close_btn.png"));

        float buttonSpacing = 10;
        float panelCenterX = panelX + panelWidth / 2;
        float panelCenterY = panelY + panelHeight / 2;

        joinSquadButtonBounds = new Rectangle(
            panelCenterX - btnWidth / 2,
            Math.abs(panelCenterY + btnHeight / 2 + buttonSpacing - GameConstants.GET_HEIGHT),

            btnWidth,
            btnHeight
        );

        leaveSquadButtonBounds = new Rectangle(
            panelCenterX - btnWidth / 2,
            Math.abs(panelCenterY + btnHeight / 2 + buttonSpacing - GameConstants.GET_HEIGHT),
            btnWidth,
            btnHeight
        );

        closePanelButtonBounds = new Rectangle(
            panelX + panelWidth - btnWidth ,
            panelY + panelHeight - btnHeight,
            btnWidth,
            btnHeight
        );

        openPanelButtonBounds = new Rectangle(
            GameConstants.GET_WIDTH - btnWidth - 10,
            GameConstants.GET_HEIGHT - btnHeight - 10,
            btnWidth,
            btnHeight
        );
    }

    public void update(float delta) {
        if (squadCountdown > 0) {
            squadCountdown -= delta;
            if (squadCountdown <= 0) {
                squadCountdown = 0;
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
        if (!isSquadPanelOpen) {
            if (openPanelButtonBounds.contains(touchX, touchY)) {
                isSquadPanelOpen = true;
            }
        } else {
            if (closePanelButtonBounds.contains(touchX, touchY)) {
                isSquadPanelOpen = false;
            } else if (!isInDungeonSquad && joinSquadButtonBounds.contains(touchX, touchY)) {
                Network.SquadAction action = new Network.SquadAction();
                action.playerId = player.getID();
                action.action = "join";
                client.sendTCP(action);
                Gdx.app.log("MultiplayerUi", "Sent join dungeon squad request for player " + player.getID());
            } else if (isInDungeonSquad && leaveSquadButtonBounds.contains(touchX, touchY)) {
                Network.SquadAction action = new Network.SquadAction();
                action.playerId = player.getID();
                action.action = "leave";
                client.sendTCP(action);
                Gdx.app.log("MultiplayerUi", "Sent leave dungeon squad request for player " + player.getID());
            }
        }
    }

    public void render(SpriteBatch batch) {
        batch.begin();
        if (!isSquadPanelOpen) {
            openPanelBtn.setBounds(
                openPanelButtonBounds.x,
                openPanelButtonBounds.y,
                openPanelButtonBounds.width,
                openPanelButtonBounds.height
            );
            openPanelBtn.draw(batch);
        } else {
            panelTexture.setBounds(panelX, panelY, panelWidth, panelHeight);
            panelTexture.draw(batch);

            if (!isInDungeonSquad) {
                btnJoinSquad.setBounds(
                    joinSquadButtonBounds.x,
                    joinSquadButtonBounds.y,
                    joinSquadButtonBounds.width,
                    joinSquadButtonBounds.height
                );
                btnJoinSquad.draw(batch);
            } else {
                btnLeaveSquad.setBounds(
                    leaveSquadButtonBounds.x,
                    leaveSquadButtonBounds.y,
                    leaveSquadButtonBounds.width,
                    leaveSquadButtonBounds.height
                );
                btnLeaveSquad.draw(batch);
            }

            btnClosePanel.setBounds(
                closePanelButtonBounds.x,
                closePanelButtonBounds.y,
                closePanelButtonBounds.width,
                closePanelButtonBounds.height
            );
            btnClosePanel.draw(batch);

            float rosterY = panelY + panelHeight - 100;
            if (isInDungeonSquad && squadCountdown > 0) {
                String countdownText = "Dungeon starts in: " + (int) Math.ceil(squadCountdown) + "s";
                font.draw(batch, countdownText, panelX + panelWidth / 2 - 50, panelY + panelHeight - 50);
            }
            if (isInDungeonSquad) {
                font.draw(batch, "Dungeon Squad:", panelX + panelWidth / 2 - 50, rosterY);
            }
            if (squadMemberNames.isEmpty()) {
                font.draw(batch, "No players in squad", panelX + panelWidth / 2 - 50, rosterY - 20);
            } else {
                for (int i = 0; i < squadMemberNames.size(); i++) {
                    font.draw(batch, squadMemberNames.get(i), panelX + panelWidth / 2 - 50, rosterY - (i + 1) * 20);
                }
            }
        }
        batch.end();
    }

    public void setSquadStatus(boolean inSquad, float countdown, ArrayList<String> memberNames) {
        this.isInDungeonSquad = inSquad;
        this.squadCountdown = countdown;
        this.squadMemberNames = memberNames != null ? new ArrayList<>(memberNames) : new ArrayList<>();
        Gdx.app.log("MultiplayerUi", "Squad status updated: inDungeonSquad=" + isInDungeonSquad + ", members=" + squadMemberNames);
    }

    public void dispose() {
        panelTexture.getTexture().dispose();
        openPanelBtn.getTexture().dispose();
        openPanelSprite.getTexture().dispose();
        btnJoinSquad.getTexture().dispose();
        btnLeaveSquad.getTexture().dispose();
        btnClosePanel.getTexture().dispose();
        font.dispose();
    }
}

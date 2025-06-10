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
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.Networking.Network;
import com.esotericsoftware.kryonet.Client;
import com.gamb1t.legacyforge.Weapons.Weapon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MultiplayerUi {
    private boolean isShopOpen = false;
    private boolean isNearShop = false;
    private boolean isSquadPanelOpen = false;
    private boolean isInDungeonSquad = false;
    private float dungeonSquadCountdown = -1;
    private ArrayList<String> dungeonSquadMemberNames = new ArrayList<>();

    private boolean isIn1v1Queue = false;
    private float oneVsOneQueueCountdown = -1;
    private ArrayList<String> oneVsOneOpponentNames = new ArrayList<>();

    private ArrayList<Weapon> weaponList;
    private Weapon selectedWeapon;
    private BitmapFont font;
    private BitmapFont scoreFont; // New font for larger score text
    private BitmapFont messageFont; // New font for larger score text
    private Sprite panelTexture;
    private Player player;
    private Client client;

    private Sprite btnJoinSquad, btnLeaveSquad, btnClosePanel, openPanelBtn, btnJoin1v1, btnLeave1v1;

    private Rectangle squadButtonBounds; // Shared bounds for join/leave squad
    private Rectangle oneVsOneButtonBounds; // Shared bounds for join/leave 1v1
    private Rectangle closePanelButtonBounds;
    private Rectangle openPanelButtonBounds;

    private float panelX, panelY, panelWidth, panelHeight;
    private float btnWidth = GameConstants.Sprite.SIZE * 4;
    private float btnHeight = GameConstants.Sprite.SIZE*4/3;

    private final GlyphLayout glyphLayout = new GlyphLayout();

    private int currentRound = 0;
    private Map<Integer, Integer> playerScores = new HashMap<>();
    private Map<Integer, String> playerNames = new HashMap<>();
    private float roundCountdownTimer = -1;
    private float goMessageTimer = -1;
    private boolean showGoMessage = false;

    public MultiplayerUi(Player player, Client client) {
        this.player = player;
        this.client = client;
        initializeRendeingObjects();
    }

    public void initializeRendeingObjects() {
        panelTexture = new Sprite(new Texture("ui/multiplayerpanelbackground.png"));

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("gothicbyte.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = GameConstants.Sprite.SIZE / 2;
        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS;
        font = generator.generateFont(parameter);
        font.getData().setScale(1f);
        font.setColor(Color.BLACK); // Set to white

        // Create a larger font for the score
        parameter.size = GameConstants.Sprite.SIZE; // Larger size for score
        scoreFont = generator.generateFont(parameter);
        scoreFont.getData().setScale(1.5f); // Further scaling
        scoreFont.setColor(Color.WHITE); // Set to white

        parameter.size = GameConstants.Sprite.SIZE;
        messageFont = generator.generateFont(parameter);
        messageFont.getData().setScale(1f);
        messageFont.setColor(Color.WHITE);


        generator.dispose();

        panelY = 0;
        panelWidth = GameConstants.GET_HEIGHT;
        panelHeight = GameConstants.GET_HEIGHT;
        panelX = (GameConstants.GET_WIDTH - panelWidth) / 2;

        btnWidth = panelWidth/2 - panelWidth/8;
        btnHeight = btnWidth/3;


        openPanelBtn = new Sprite(new Texture("ui/play_btn.png"));
        btnJoinSquad = new Sprite(new Texture("ui/join_btn.png"));
        btnLeaveSquad = new Sprite(new Texture("ui/leave_btn.png"));
        btnClosePanel = new Sprite(new Texture("ui/close_btn.png"));
        btnJoin1v1 = new Sprite(new Texture("ui/join_btn.png"));
        btnLeave1v1 = new Sprite(new Texture("ui/leave_btn.png"));

        float leftButtonX = panelX + panelWidth / 4 - btnWidth / 2 + panelWidth/16;
        float rightButtonX = panelX + panelWidth * 3 / 4 - btnWidth / 2 - panelWidth/16;
        float buttonY = GameConstants.Sprite.SIZE;

        squadButtonBounds = new Rectangle(leftButtonX, buttonY, btnWidth, btnHeight);
        oneVsOneButtonBounds = new Rectangle(rightButtonX, buttonY, btnWidth, btnHeight);

        closePanelButtonBounds = new Rectangle(
            panelX + panelWidth - GameConstants.Sprite.SIZE * 2,
            panelY + panelHeight - GameConstants.Sprite.SIZE * 2,
            GameConstants.Sprite.SIZE * 2,
            GameConstants.Sprite.SIZE * 2
        );

        openPanelButtonBounds = new Rectangle(
            GameConstants.GET_WIDTH / 2 - btnWidth / 2,
            GameConstants.GET_HEIGHT - btnHeight - GameConstants.Sprite.SIZE,
            btnWidth,
            btnHeight
        );
    }

    public void update(float delta) {
        if (roundCountdownTimer > 0) {
            roundCountdownTimer -= delta;
            if (roundCountdownTimer <= 0) {
                roundCountdownTimer = 0;
                showGoMessage = true;
                goMessageTimer = 3;
            }
        }

        if (goMessageTimer > 0) {
            goMessageTimer -= delta;
            if (goMessageTimer <= 0) {
                goMessageTimer = 0;
                showGoMessage = false;
            }
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
            } else if (squadButtonBounds.contains(touchX, touchY)) {
                if (!isInDungeonSquad) {
                    Network.SquadAction action = new Network.SquadAction();
                    action.playerId = player.getID();
                    action.action = "join";
                    client.sendTCP(action);
                    Gdx.app.log("MultiplayerUi", "Sent join dungeon squad request for player " + player.getID());
                } else {
                    Network.SquadAction action = new Network.SquadAction();
                    action.playerId = player.getID();
                    action.action = "leave";
                    client.sendTCP(action);
                    Gdx.app.log("MultiplayerUi", "Sent leave dungeon squad request for player " + player.getID());
                }
            } else if (oneVsOneButtonBounds.contains(touchX, touchY)) {
                if (!isIn1v1Queue) {
                    Network.SquadAction action = new Network.SquadAction();
                    action.playerId = player.getID();
                    action.action = "join1v1";
                    client.sendTCP(action);
                    Gdx.app.log("MultiplayerUi", "Sent join 1v1 queue request for player " + player.getID());
                } else if (isIn1v1Queue && oneVsOneOpponentNames.isEmpty()) {
                    Network.SquadAction action = new Network.SquadAction();
                    action.playerId = player.getID();
                    action.action = "leave1v1";
                    client.sendTCP(action);
                    Gdx.app.log("MultiplayerUi", "Sent leave 1v1 queue request for player " + player.getID());
                }
            }
        }
    }

    public void updateHubCountdiwns(float delta){
        if (dungeonSquadCountdown > 0) {
            dungeonSquadCountdown -= delta;
            if (dungeonSquadCountdown <= 0) dungeonSquadCountdown = 0;
        }
        if (oneVsOneQueueCountdown > 0) {
            oneVsOneQueueCountdown -= delta;
            if (oneVsOneQueueCountdown <= 0) oneVsOneQueueCountdown = 0;
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

            // Draw squad button
            if (!isInDungeonSquad) {
                btnJoinSquad.setBounds(squadButtonBounds.x, squadButtonBounds.y, squadButtonBounds.width, squadButtonBounds.height);
                btnJoinSquad.draw(batch);
            } else {
                btnLeaveSquad.setBounds(squadButtonBounds.x, squadButtonBounds.y, squadButtonBounds.width, squadButtonBounds.height);
                btnLeaveSquad.draw(batch);
            }

            // Draw 1v1 button
            if (!isIn1v1Queue) {
                btnJoin1v1.setBounds(oneVsOneButtonBounds.x, oneVsOneButtonBounds.y, oneVsOneButtonBounds.width, oneVsOneButtonBounds.height);
                btnJoin1v1.draw(batch);
            } else if (isIn1v1Queue && oneVsOneOpponentNames.isEmpty()) {
                btnLeave1v1.setBounds(oneVsOneButtonBounds.x, oneVsOneButtonBounds.y, oneVsOneButtonBounds.width, oneVsOneButtonBounds.height);
                btnLeave1v1.draw(batch);
            }

            btnClosePanel.setBounds(
                closePanelButtonBounds.x,
                closePanelButtonBounds.y,
                closePanelButtonBounds.width,
                closePanelButtonBounds.height
            );
            btnClosePanel.draw(batch);

            // Render dungeon squad texts
            if (isInDungeonSquad) {
                float textY = panelY + panelHeight / 2 + panelHeight/24;
                if (dungeonSquadCountdown > 0) {
                    String countdownText = (int) Math.ceil(dungeonSquadCountdown) + "s";
                    glyphLayout.setText(font, countdownText);
                    float textWidth = glyphLayout.width;
                    float textX = squadButtonBounds.x + squadButtonBounds.width / 2 - textWidth / 2;
                    font.draw(batch, countdownText, textX, textY);
                    textY -= font.getLineHeight();
                }
                String squadText = "Players In squad";
                glyphLayout.setText(font, squadText);
                float textWidth = glyphLayout.width;
                float textX = squadButtonBounds.x + squadButtonBounds.width / 2 - textWidth / 2;
                font.draw(batch, squadText, textX, textY);
                textY -= font.getLineHeight();
                for (String name : dungeonSquadMemberNames) {
                    glyphLayout.setText(font, name);
                    textWidth = glyphLayout.width;
                    textX = squadButtonBounds.x + squadButtonBounds.width / 2 - textWidth / 2;
                    font.draw(batch, name, textX, textY);
                    textY -= font.getLineHeight();
                }
            }

            if (isIn1v1Queue) {
                float textY = panelHeight/2 ;
                if (oneVsOneOpponentNames.isEmpty()) {
                    String queueText = "In queue";
                    glyphLayout.setText(font, queueText);
                    float textWidth = glyphLayout.width;
                    float textX = oneVsOneButtonBounds.x + oneVsOneButtonBounds.width / 2 - textWidth / 2;
                    font.draw(batch, queueText, textX, textY);
                } else {
                    String foundText = "Found player:" + oneVsOneOpponentNames.get(0);
                    glyphLayout.setText(font, foundText);
                    float textWidth = glyphLayout.width;
                    float textX = oneVsOneButtonBounds.x + oneVsOneButtonBounds.width / 2 - textWidth / 2;
                    font.draw(batch, foundText, textX, textY);
                    if (oneVsOneQueueCountdown > 0) {
                        textY -= panelHeight/16;
                        String startingText = "Starting in: " + (int) Math.ceil(oneVsOneQueueCountdown) + "s";
                        glyphLayout.setText(font, startingText);
                        textWidth = glyphLayout.width;
                        textX = oneVsOneButtonBounds.x + oneVsOneButtonBounds.width / 2 - textWidth / 2;
                        font.draw(batch, startingText, textX, textY);
                    }
                }
            }
        }
        batch.end();
    }

    String countdownText = "Round starts in: ";

    public void renderRoundCountdown(SpriteBatch batch) {
        batch.begin();
        if (roundCountdownTimer > 0) {
            String countdownText = this.countdownText + (int) Math.ceil(roundCountdownTimer) + " s";
            glyphLayout.setText(messageFont, countdownText);
            float textWidth = glyphLayout.width;
            float textX = (GameConstants.GET_WIDTH - textWidth) / 2;
            messageFont.draw(batch, countdownText, textX, GameConstants.GET_HEIGHT-GameConstants.Sprite.SIZE*2 - scoreFont.getLineHeight());
        } else if (showGoMessage && goMessageTimer > 0) {
            if(countdownText.equals("Round starts in: ")){
            String goText = "GO";
            glyphLayout.setText(scoreFont, goText);
            float textWidth = glyphLayout.width;
            float textX = (GameConstants.GET_WIDTH - textWidth) / 2;
            scoreFont.draw(batch, goText, textX, GameConstants.GET_HEIGHT-GameConstants.Sprite.SIZE - scoreFont.getLineHeight());
        }

        }
        batch.end();
    }



    public void renderScore(SpriteBatch batch) {
        batch.begin();
        if (!playerScores.isEmpty()) {
            StringBuilder scoreText = new StringBuilder();

            Integer mainPlayerScore = playerScores.get(player.getID());
            if (mainPlayerScore != null) {
                scoreText.append(mainPlayerScore);
            } else {
                scoreText.append("0");
            }

            Integer opponentScore = null;
            for (Map.Entry<Integer, Integer> entry : playerScores.entrySet()) {
                if (!entry.getKey().equals(player.getID())) {
                    opponentScore = entry.getValue();
                    break;
                }
            }
            scoreText.append(" : ");
            if (opponentScore != null) {
                scoreText.append(opponentScore);
            } else {
                scoreText.append("0");
            }

            glyphLayout.setText(scoreFont, scoreText.toString());
            float textWidth = glyphLayout.width;
            float textX = (GameConstants.GET_WIDTH - textWidth) / 2;
            scoreFont.draw(batch, scoreText.toString(), textX, GameConstants.GET_HEIGHT - 20);
        }
        batch.end();
    }

    public void setDungeonSquadStatus(boolean inSquad, float countdown, ArrayList<String> memberNames) {
        this.isInDungeonSquad = inSquad;
        this.dungeonSquadCountdown = countdown;
        this.dungeonSquadMemberNames = memberNames != null ? new ArrayList<>(memberNames) : new ArrayList<>();
        Gdx.app.log("MultiplayerUi", "Dungeon squad status updated: inDungeonSquad=" + isInDungeonSquad + ", members=" + dungeonSquadMemberNames);
    }

    public void setOneVsOneQueueStatus(boolean inQueue, float countdown, ArrayList<String> opponentNames) {
        this.isIn1v1Queue = inQueue;
        this.oneVsOneQueueCountdown = countdown;
        this.oneVsOneOpponentNames = opponentNames != null ? new ArrayList<>(opponentNames) : new ArrayList<>();
        if (!opponentNames.isEmpty()) {
            playerNames.clear();
            playerNames.put(player.getID(), player.getName());
        }
        Gdx.app.log("MultiplayerUi", "1v1 queue status updated: in1v1Queue=" + isIn1v1Queue + ", opponents=" + oneVsOneOpponentNames);
    }

    public void handleRoundStart(Network.RoundStart roundStart) {
        this.currentRound = roundStart.roundNumber;
        this.playerScores = new HashMap<>(roundStart.playerScores);
        this.showGoMessage = false;
        this.goMessageTimer = 0;

        playerNames.putIfAbsent(player.getID(), player.getName());
        if (playerNames.size() < 2) {
            for (Integer id : playerScores.keySet()) {
                if (id != player.getID() && !playerNames.containsKey(id)) {
                    String opponentName = oneVsOneOpponentNames.isEmpty() ? "Opponent " + id : oneVsOneOpponentNames.get(0);
                    playerNames.put(id, opponentName);
                }
            }
        }

        int scoreSum = 0;
        for (Integer score : playerScores.values()) {
            scoreSum += score;
        }

        if (scoreSum == 5) {
            Integer mainPlayerScore = playerScores.get(player.getID());
            Integer opponentScore = null;
            for (Map.Entry<Integer, Integer> entry : playerScores.entrySet()) {
                if (!entry.getKey().equals(player.getID())) {
                    opponentScore = entry.getValue();
                    break;
                }
            }
            if (mainPlayerScore != null && opponentScore != null && mainPlayerScore > opponentScore) {
                countdownText = "      You Won\n Going back in:";
            } else {
                countdownText = "      You Lost\n Going back in:";
            }
            roundCountdownTimer = 3;
        } else {
            countdownText = "Round starts in: ";
            roundCountdownTimer = 3;
        }

        Gdx.app.log("MultiplayerUi", "Round " + currentRound + " started, scores: " + playerScores);
    }


    public void dispose() {
        panelTexture.getTexture().dispose();
        openPanelBtn.getTexture().dispose();
        btnJoinSquad.getTexture().dispose();
        btnLeaveSquad.getTexture().dispose();
        btnClosePanel.getTexture().dispose();
        btnJoin1v1.getTexture().dispose();
        btnLeave1v1.getTexture().dispose();
        font.dispose();
        scoreFont.dispose();
    }
}

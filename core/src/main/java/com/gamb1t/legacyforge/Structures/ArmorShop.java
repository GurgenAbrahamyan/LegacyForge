package com.gamb1t.legacyforge.Structures;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.ManagerClasses.ArmorLoader;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;
import com.gamb1t.legacyforge.ManagerClasses.TouchManager;
import com.gamb1t.legacyforge.Weapons.Armor;

import java.util.ArrayList;

public class ArmorShop {
    private boolean isShopOpen = false;
    private boolean isNearShop = false;

    private float shopX, shopY;
    private float shopWidth, shopHeight;
    private Polygon shopHitbox;
    private Sprite shopSprite;
    private ArrayList<Armor> armorList;
    private Armor selectedArmor;
    private BitmapFont font;
    private Sprite panelTexture;
    private Player player;
    private TouchManager touchManager;

    private Sprite btnBuyAvailable, btnBuyUnavailable, btnEquip, btnEquipped;

    private float panelTile;

    private String shopTexture;
    private String armorPath;

    private ArmorLoader armorLoader;

    float btnWidth = GameConstants.Sprite.SIZE * 2;
    float btnHeight = GameConstants.Sprite.SIZE;

    private float showPannelX, showPannelY, panelWidth, panelHeight;

    private Rectangle openShopButtonBounds;
    private Sprite openShopButtonSprite;

    public ArmorShop(float shopX, float shopY, float shopWidth, float shopHeight, String shopTexture, ArmorLoader wp, Player player, TouchManager touchManager) {
        this.shopX = shopX;
        this.shopY = shopY;
        this.shopWidth = shopWidth;
        this.shopHeight = shopHeight;
        this.shopHitbox = new Polygon(new float[]{
            shopX, shopY,
            shopX, shopY,
            shopX + shopWidth - 2 * GameConstants.Sprite.SIZE, shopY,
            shopX + shopWidth - 2 * GameConstants.Sprite.SIZE, shopY + GameConstants.Sprite.SIZE,
            shopX, shopY + shopHeight
        });
        this.shopTexture = shopTexture;

        this.armorLoader = wp;
        this.armorList = wp.getArmorList();
        this.player = player;
        this.touchManager = touchManager;
        this.armorPath = wp.getResourcePath(); // Assuming getResourcePath() exists in ArmorLoader
    }

    public void initializeRenderingObjects() {
        this.shopSprite = new Sprite(new Texture(shopTexture));
        this.shopSprite.setSize(shopWidth, shopHeight);

        this.panelTexture = new Sprite(new Texture("shops/armor_shop_ui.png"));
        this.font = new BitmapFont();

        showPannelY = 0;
        panelWidth = GameConstants.GET_HEIGHT;
        panelHeight = GameConstants.GET_HEIGHT;
        showPannelX = (float) (GameConstants.GET_WIDTH / 2) - panelWidth / 2;

        panelTile = panelWidth / 32f;

        openShopButtonSprite = new Sprite(new Texture("shops/open_shop_button.png"));
        btnBuyAvailable = new Sprite(new Texture("shops/buy_available.png"));
        btnBuyUnavailable = new Sprite(new Texture("shops/buy_unavailable.png"));
        btnEquip = new Sprite(new Texture("shops/equip.png"));
        btnEquipped = new Sprite(new Texture("shops/equipped.png"));
    }

    public void update(Polygon playerPolygon) {
        if (Intersector.overlapConvexPolygons(playerPolygon, shopHitbox)) {
            isNearShop = true;
        } else {
            isNearShop = false;
            closeShopUI();
        }
    }

    public void draw(SpriteBatch batch, float cameraX, float cameraY) {
        batch.draw(shopSprite, shopX + cameraX - GameConstants.Sprite.SIZE, shopY + cameraY + GameConstants.Sprite.SIZE, GameConstants.Sprite.SIZE * 4, GameConstants.Sprite.SIZE * 3);
    }

    public void drawShopUi(SpriteBatch batch) {
        batch.begin();

        if (isNearShop && !isShopOpen) {
            openShopButtonBounds = new Rectangle(
                GameConstants.GET_WIDTH / 2f - btnWidth / 2f,
                GameConstants.GET_HEIGHT / 4f - btnHeight,
                btnWidth,
                btnHeight
            );
            batch.draw(openShopButtonSprite, openShopButtonBounds.x, Math.abs(openShopButtonBounds.y), openShopButtonBounds.width, openShopButtonBounds.height);
        }

        if (isShopOpen) {
            openShopUI(batch);
        }

        batch.end();
    }

    private void openShopUI(SpriteBatch batch) {
        drawShopPanel(batch);
        drawArmorCards(batch);
    }

    private void closeShopUI() {
        isShopOpen = false;
    }

    private void drawShopPanel(SpriteBatch batch) {
        batch.draw(panelTexture, showPannelX, showPannelY, panelWidth, panelHeight);
    }

    int MAX_SLOTS = 8; // 2 rows, 4 columns

    private void drawArmorCards(SpriteBatch batch) {
        float startX = showPannelX + panelTile;
        float startY = showPannelY + panelTile;
        float btnW = panelTile * 6;
        float btnH = panelTile * 2;
        float colOffset = panelWidth / 4f; // 4 columns
        float rowOffset = panelHeight / 2f;

        for (int slot = 0; slot < MAX_SLOTS; slot++) {
            int col = slot % 4; // 4 columns
            int row = slot / 4; // 0 = top row, 1 = bottom row
            float x = startX + col * colOffset;
            float y = startY + (1 - row) * rowOffset; // Invert row: row 0 at top

            Sprite tex;
            if (slot >= armorList.size()) {
                tex = btnBuyUnavailable;
            } else {
                Armor a = armorList.get(slot);
                boolean owns = player.getInventory().containsArmorByName(a.getName());
                boolean equipped = owns && isEquipped(a);
                boolean canBuy = !owns && player.getMoney() >= a.getPrice();

                if (owns) {
                    tex = equipped ? btnEquipped : btnEquip;
                } else {
                    tex = canBuy ? btnBuyAvailable : btnBuyUnavailable;
                }
            }

            batch.draw(tex, x, y, btnW, btnH);
        }
    }

    private boolean isEquipped(Armor armor) {
        String type = armor.getType();
        if (type.equals("helmet")) {
            return player.getEquipment().getHelmet() != null && player.getEquipment().getHelmet().getName().equals(armor.getName());
        } else if (type.equals("chestplate")) {
            return player.getEquipment().getChestplate() != null && player.getEquipment().getChestplate().getName().equals(armor.getName());
        }
        return false;
    }

    public void handleTouchInput(float touchX, float touchY) {
        touchY = GameConstants.GET_HEIGHT - touchY;

        if (isNearShop && !isShopOpen && openShopButtonBounds.contains(touchX, touchY)) {
            isShopOpen = true;
            return;
        }
        if (!isShopOpen) return;

        float startX = showPannelX + panelTile;
        float startY = showPannelY + panelTile;
        float btnW = panelTile * 6;
        float btnH = panelTile * 2;
        float colOffset = panelWidth / 4f; // 4 columns
        float rowOffset = panelHeight / 2f;

        for (int slot = 0; slot < MAX_SLOTS; slot++) {
            int col = slot % 4;
            int row = slot / 4;
            float x = startX + col * colOffset;
            float y = startY + (1 - row) * rowOffset;

            if (touchX >= x && touchX <= x + btnW && touchY >= y && touchY <= y + btnH) {
                if (slot < armorList.size()) {
                    Armor a = armorList.get(slot);
                    boolean owns = player.getInventory().containsArmorByName(a.getName());
                    boolean equipped = owns && isEquipped(a);
                    boolean canBuy = !owns && player.getMoney() >= a.getPrice();

                    if (owns && !equipped) {
                        player.equipArmor(player.getInventory().getArmorByName(a.getName()));
                        selectedArmor = a;
                    } else if (!owns && canBuy) {
                        player.addMoney(-a.getPrice());
                        player.addArmor(armorLoader.deepCopyArmor(a.getName()), 1);
                    }
                }
                break;
            }
        }
    }

    private void equipArmor(Armor armor) {
        player.equipArmor(armor);
    }

    // Getter methods
    public float getShopX() {
        return shopX;
    }

    public float getShopY() {
        return shopY;
    }

    public float getShopHeight() {
        return shopHeight;
    }

    public float getShopWidth() {
        return shopWidth;
    }

    public ArrayList<Armor> getArmorList() {
        return armorList;
    }

    public String getArmorPath() {
        return armorPath;
    }

    public String getShopTexture() {
        return shopTexture;
    }

    public Polygon getShopHitbox() {
        return shopHitbox;
    }

    public boolean isShopOpen() {
        return isShopOpen;
    }
}

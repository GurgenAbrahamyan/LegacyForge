package com.gamb1t.legacyforge.Structures;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;
import com.gamb1t.legacyforge.ManagerClasses.TouchManager;
import com.gamb1t.legacyforge.Weapons.Weapon;
import com.badlogic.gdx.math.Rectangle;


import java.util.ArrayList;

public class Shop {
    private boolean isShopOpen = false;
    private float shopX, shopY;
    private float shopWidth, shopHeight;
    private Polygon shopHitbox;
    private Sprite shopSprite;
    private float scrollX = 0; // For horizontal scroll
    private ArrayList<Weapon> weaponList;
    private Weapon selectedWeapon;
    private BitmapFont font;
    private Sprite panelTexture;
    private Player player;
    private Sprite boarderTexture;
    private TouchManager touchManager;

    private float showPannelX, showPannelY, panelWidth, panelHeight;

    public Shop(float shopX, float shopY, float shopWidth, float shopHeight, String shopTexture, ArrayList<Weapon> weapons, Player player, TouchManager touchManager) {
        this.shopX = shopX;
        this.shopY = shopY;
        this.shopWidth = shopWidth;
        this.shopHeight = shopHeight;
        this.shopHitbox = new Polygon(new float[]{shopX, shopY,
            shopX, shopY,
            shopX + shopWidth - 2 * GameConstants.Sprite.SIZE, shopY,
            shopX + shopWidth - 2 * GameConstants.Sprite.SIZE, shopY + GameConstants.Sprite.SIZE,
            shopX, shopY + shopHeight});
        this.shopSprite = new Sprite(new Texture(shopTexture));
        this.shopSprite.setSize(shopWidth, shopHeight);
        this.weaponList = weapons;
        this.font = new BitmapFont();
        this.player = player;
        panelTexture = new Sprite(new Texture("shops/shop_panel_background.png"));
        showPannelX= (float) (GameConstants.GET_WIDTH / 6);
        showPannelY = 0;
        panelWidth = (float) (GameConstants.GET_WIDTH / 1.5);
        panelHeight = GameConstants.GET_HEIGHT;
        boarderTexture = new Sprite(new Texture("shops/selected_border.png"));
        this.touchManager = touchManager;
    }

    public void update(Polygon playerPolygon) {
        if (Intersector.overlapConvexPolygons(playerPolygon, shopHitbox)) {
            isShopOpen = true;
        } else {
            if (isShopOpen) {
                closeShopUI();
            }
        }

    }

    public void draw(SpriteBatch batch, float cameraX, float cameraY) {
        batch.draw(shopSprite, shopX + cameraX - GameConstants.Sprite.SIZE, shopY + cameraY + GameConstants.Sprite.SIZE, GameConstants.Sprite.SIZE * 4, GameConstants.Sprite.SIZE * 3);
    }

    public void drawShopUi(SpriteBatch batch) {
        if (isShopOpen) {
            openShopUI(batch);
        }
    }

    private void openShopUI(SpriteBatch batch) {
        drawShopPanel(batch);
        drawWeaponCards(batch);
    }

    private void closeShopUI() {
        isShopOpen = false;
    }

    private void drawShopPanel(SpriteBatch batch) {
        batch.draw(panelTexture,  showPannelX, showPannelY, panelWidth, panelHeight);
    }

    private void drawWeaponCards(SpriteBatch batch) {
        int weaponCount = weaponList.size();
        float scaleMultiplier = panelWidth / (weaponCount * GameConstants.Sprite.SIZE * 3); // Adjust based on total space

        for (int i = 0; i < weaponCount; i++) {
            Weapon weapon = weaponList.get(i);

            float x = showPannelX + i * (panelWidth / weaponCount);
            float y = panelHeight / 2 - (GameConstants.Sprite.SIZE * scaleMultiplier);

            float spriteSize = GameConstants.Sprite.SIZE * 2 * scaleMultiplier;
            float borderSize = GameConstants.Sprite.SIZE * 2.5f * scaleMultiplier;

            batch.draw(new Texture(weapon.getSprite()), x, y, spriteSize, spriteSize);

            font.getData().setScale(GameConstants.Sprite.SIZE/30);
            float offset = GameConstants.Sprite.SIZE/3;
            font.draw(batch, weapon.getName(), x+offset, y + spriteSize * 1.25f);
            font.draw(batch, "Price: " + (weapon.getPrice() > 0 ? weapon.getPrice() : "Owned"), x+offset, y + spriteSize * 1.1f);
            font.draw(batch, "Damage: " + weapon.getDamage(), x+offset, y + spriteSize * 0.9f);
            font.draw(batch, "Attack Speed: " + weapon.getAttackSpeed(), x+offset, y + spriteSize * 0.7f);

            if (selectedWeapon == weapon) {
                batch.draw(boarderTexture, x, 0 , borderSize, panelHeight);
            }
        }

        // Reset font scale after drawing
        font.getData().setScale(1);
    }




    public void handleTouchInput(float touchX, float touchY) {
        if (isShopOpen) {
            int weaponCount = weaponList.size();
            float scaleMultiplier = panelWidth / (weaponCount * GameConstants.Sprite.SIZE * 3);

            for (int i = 0; i < weaponCount; i++) {
                Weapon weapon = weaponList.get(i);

                float x = showPannelX + i * (panelWidth / weaponCount);
                float y = panelHeight / 2 - (GameConstants.Sprite.SIZE * scaleMultiplier);

                float cardWidth = GameConstants.Sprite.SIZE * 2 * scaleMultiplier;
                float cardHeight = GameConstants.Sprite.SIZE * 2 * scaleMultiplier;

                Rectangle cardHitbox = new Rectangle(x, y, cardWidth, cardHeight);

                if (cardHitbox.contains(touchX, touchY)) {
                    if (weapon.getPrice() > 0 && player.getMoney() >= weapon.getPrice()) {
                        buyWeapon(weapon);
                    } else if (weapon.getPrice() == 0) {
                        setWeapon(weapon);
                    }
                    break;
                }
            }
        }
    }

    private void buyWeapon(Weapon weapon) {
        player.addMoney(-weapon.getPrice());
        weapon.setPrice(0);
        setWeapon(weapon);
    }

    private void setWeapon(Weapon weapon) {
        selectedWeapon = weapon;
        touchManager.setWeapon(selectedWeapon);
        player.setCurrentWeapon(selectedWeapon);
    }

    public Polygon getShopHitbox() {
        return shopHitbox;
    }

    public boolean isShopOpen() {
        return isShopOpen;
    }
}

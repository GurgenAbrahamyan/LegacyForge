package Structures;

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
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Gdx;
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

    private float showPannelX, showPannelY;

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
        showPannelY = (float) (GameConstants.GET_WIDTH / 1.5);
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
        batch.draw(panelTexture,  showPannelX, 0, showPannelY,GameConstants.GET_HEIGHT);
    }

    private void drawWeaponCards(SpriteBatch batch) {
        for (int i = 0; i < weaponList.size(); i++) {
            Weapon weapon = weaponList.get(i);
            float x = GameConstants.Sprite.SIZE * 4 + (i * (GameConstants.Sprite.SIZE * 2));
            float y = GameConstants.Sprite.SIZE * 2;

            batch.draw(new Texture(weapon.getSprite()), x, y, GameConstants.Sprite.SIZE * 2, GameConstants.Sprite.SIZE * 2);

            GlyphLayout nameLayout = new GlyphLayout(font, weapon.getName());
            GlyphLayout priceLayout = new GlyphLayout(font, "Price: " + (weapon.getPrice() > 0 ? weapon.getPrice() : "Owned"));
            GlyphLayout damageLayout = new GlyphLayout(font, "Damage: " + weapon.getDamage());
            GlyphLayout attackSpeedLayout = new GlyphLayout(font, "Attack Speed: " + weapon.getAttackSpeed());

            font.draw(batch, nameLayout, x, y + GameConstants.Sprite.SIZE * 2.5f);
            font.draw(batch, priceLayout, x, y + GameConstants.Sprite.SIZE * 2.2f);
            font.draw(batch, damageLayout, x, y + GameConstants.Sprite.SIZE * 1.9f);
            font.draw(batch, attackSpeedLayout, x, y + GameConstants.Sprite.SIZE * 1.6f);

            if (selectedWeapon == weapon) {
                batch.draw(boarderTexture, x - GameConstants.Sprite.SIZE / 4, y - GameConstants.Sprite.SIZE / 4, GameConstants.Sprite.SIZE * 2.5f, GameConstants.Sprite.SIZE * 2.5f);
            }
        }
    }


    public void handleTouchInput(float touchX, float touchY) {
        if(isShopOpen){
        System.out.println(touchX);
        System.out.println(touchY);
        for (int i = 0; i < weaponList.size(); i++) {
            Weapon weapon = weaponList.get(i);
            float x = GameConstants.GET_WIDTH / 4 + (i * (GameConstants.Sprite.SIZE * 2)) + scrollX;
            float y = GameConstants.GET_HEIGHT / 4 + GameConstants.Sprite.SIZE;

            Rectangle cardHitbox = new Rectangle(x, y, GameConstants.Sprite.SIZE * 2, GameConstants.Sprite.SIZE * 2);
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

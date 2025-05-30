package com.gamb1t.legacyforge.Structures;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.gamb1t.legacyforge.Entity.Player;
import com.gamb1t.legacyforge.ManagerClasses.GameConstants;
import com.gamb1t.legacyforge.ManagerClasses.TouchManager;
import com.gamb1t.legacyforge.ManagerClasses.WeaponLoader;
import com.gamb1t.legacyforge.Weapons.Weapon;

import java.util.ArrayList;

public class Shop {
    private boolean isShopOpen = false;
    private boolean isNearShop = false;

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
    private TouchManager touchManager;

    private Sprite btnBuyAvailable, btnBuyUnavailable, btnEquip, btnEquipped;

    private float panelTile;

    private String shopTexture;
    private  String weaponPath;

    private WeaponLoader weaponLoader;




    float btnWidth = GameConstants.Sprite.SIZE * 2;
    float btnHeight = GameConstants.Sprite.SIZE;

    private float showPannelX, showPannelY, panelWidth, panelHeight;

    private Rectangle openShopButtonBounds;
    private Sprite openShopButtonSprite;

    public Shop(float shopX, float shopY, float shopWidth, float shopHeight, String shopTexture, WeaponLoader wp, Player player, TouchManager touchManager) {
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

        weaponLoader = wp;

        this.weaponList = wp.getWeaponList();



        this.player = player;

        this.touchManager = touchManager;

        this.weaponPath = wp.getRecourcePath();



    }
    public void initializeRendeingObjects(){

        this.shopSprite = new Sprite(new Texture(shopTexture));
        this.shopSprite.setSize(shopWidth, shopHeight);

        this.panelTexture = new Sprite(new Texture("shops/shop_ui.png"));
        this.font = new BitmapFont();

        showPannelY = 0;
        panelWidth =  GameConstants.GET_HEIGHT;
        panelHeight = GameConstants.GET_HEIGHT;
        showPannelX = (float) (GameConstants.GET_WIDTH / 2) - panelWidth/2;

        panelTile = panelWidth / 32f;

        openShopButtonSprite = new Sprite(new Texture("shops/open_shop_button.png"));
        btnBuyAvailable   = new Sprite(new Texture("shops/buy_available.png"));
        btnBuyUnavailable = new Sprite(new Texture("shops/buy_unavailable.png"));
        btnEquip          = new Sprite(new Texture("shops/equip.png"));
        btnEquipped       = new Sprite(new Texture("shops/equipped.png"));

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

            System.out.println(openShopButtonBounds.x+" "+ openShopButtonBounds.y);
        }

        if (isShopOpen) {
            openShopUI(batch);
        }

        batch.end();
    }

    private void openShopUI(SpriteBatch batch) {
        drawShopPanel(batch);
        drawWeaponCards(batch);
    }

    private void closeShopUI() {
        isShopOpen = false;
    }

    private void drawShopPanel(SpriteBatch batch) {
        batch.draw(panelTexture, showPannelX, showPannelY, panelWidth, panelHeight);
    }
    int MAX_SLOTS = 8;

    private void drawWeaponCards(SpriteBatch batch) {
        float startX    = showPannelX + panelTile;
        float startY    = showPannelY + panelTile;
        float btnW      = panelTile * 6;
        float btnH      = panelTile * 2;
        float colOffset = panelWidth / 4f;
        float rowOffset = panelHeight / 2f;

        for (int slot = 0; slot < MAX_SLOTS; slot++) {
            int col = slot % 4;
            int row = slot / 4;
            float x = startX + col * colOffset;
            float y = startY + (1 - row) * rowOffset;

            Sprite tex;
            if (slot >= weaponList.size()) {
                tex = btnBuyUnavailable;
            } else {
                Weapon w = weaponList.get(slot);
                boolean owns     = player.getInventory().containsWeaponByName(w.getName());
                boolean equipped = owns && player.getCurrentWeapon().getName().equals(w.getName());
                boolean canBuy   = !owns && player.getMoney() >= w.getPrice();

                if (owns) {
                    tex = equipped ? btnEquipped : btnEquip;
                } else {
                    tex = canBuy ? btnBuyAvailable : btnBuyUnavailable;
                }
            }

            batch.draw(tex, x, y, btnW, btnH);
        }
    }



    public void handleTouchInput(float touchX, float touchY) {

            touchY = GameConstants.GET_HEIGHT - touchY;



        if (isNearShop && !isShopOpen
            && openShopButtonBounds.contains(touchX, touchY)) {
            isShopOpen = true;
            return;
        }
        if (!isShopOpen) return;

        float startX    = showPannelX + panelTile;
        float startY    = showPannelY + panelTile;
        float btnW      = panelTile * 6;
        float btnH      = panelTile * 2;
        float colOffset = panelWidth / 4f;
        float rowOffset = panelHeight / 2f;

        for (int slot = 0; slot < MAX_SLOTS; slot++) {
            int col = slot % 4;
            int row = slot / 4;
            float x = startX + col * colOffset;
            float y = startY + (1 - row) * rowOffset;  // <— same invert

            if (touchX >= x && touchX <= x + btnW
                && touchY >= y && touchY <= y + btnH) {

                if (slot < weaponList.size()) {
                    Weapon w = weaponList.get(slot);
                    boolean owns     = player.getInventory().containsWeaponByName(w.getName());
                    boolean equipped = owns && player.getCurrentWeapon().getName().equals(w.getName());
                    boolean canBuy   = !owns && player.getMoney() >= w.getPrice();

                    if (owns && !equipped) {
                        equipWeapon(player.getInventory().getWeaponByName(w.getName()));
                        selectedWeapon = w;
                    } else if (!owns && canBuy) {
                        player.addMoney(-w.getPrice());
                        player.addWeapon(weaponLoader.deepCopyWeapon(w.getName()), 1);
                    }
                }
                break;
            }
        }
    }


    private void buyWeapon(Weapon weapon, int level) {
        player.addMoney(-weapon.getPrice());
        addWeapon(weapon, level);
    }

    private void addWeapon(Weapon weapon, int level) {

        player.addWeapon(weapon, level);


    }

    public void equipWeapon(Weapon weapon){
        touchManager.setWeapon(weapon);
        player.setCurrentWeapon(weapon);

    }



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
    public ArrayList<Weapon> getWeaponList(){
        return weaponList;
    }

    public String getWeaponPath() {
        return weaponPath;
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

package td.core.ui;

import td.core.model.board.Board;
import td.core.model.cards.Card;
import td.core.model.cards.BuildTowerCard;
import td.core.model.towers.DamageBuffDecorator;
import td.core.model.enemies.Enemy;
import td.core.model.game.GameManager;
import td.core.MainGame;
import td.core.model.towers.PendingTower;
import td.core.model.game.Projectile;
import td.core.model.towers.RangeBuffDecorator;
import td.core.model.cards.StrategyCardFactory;
import td.core.model.board.Tile;
import td.core.model.towers.TowerComponent;
import td.core.model.towers.TowerDecorator;
import td.core.model.towers.UpgradeDecorator;
import td.core.model.phases.GameOverPhase;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
public class GameScreen implements Screen {
    private final MainGame game;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final Texture backgroundTexture;

    private final float tileSize = 28f;
    private final Vector2 boardOrigin = new Vector2(40f, 40f);
    private final float hudPanelX = 700f;
    private final float hudPanelY = 24f;
    private final float hudPanelWidth = 460f;
    private final float hudPanelHeight = 620f;
    private final float statsBoxY = 468f;
    private final float statsBoxHeight = 160f;
    private final float handTitleY = 414f;
    private final float cardsTopY = 326f;
    private final float cardHeight = 64f;
    private final float cardGap = 10f;
    private final float cardColumnGap = 8f;
    private TowerComponent hoveredTower;
    private TowerComponent selectedTower;
    private Vector2 mouseWorld = new Vector2();
    private int hoverGridX = -1;
    private int hoverGridY = -1;

    public GameScreen(MainGame game) {
        this.game = game;
        camera.setToOrtho(false, 1200f, 700f);
        backgroundTexture = createPixelBackground();

        GameManager manager = GameManager.get();
        manager.init(Board.createDefault(20, 20, tileSize), new StrategyCardFactory());
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new GameInputProcessor());
    }

    @Override
    public void render(float delta) {
        GameManager manager = GameManager.get();
        manager.update(delta);

        Gdx.gl.glClearColor(0.08f, 0.09f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        drawBackground();
        drawBoard(manager.getBoard());
        drawEntities(manager);
        drawPlacementPreview(manager);
        drawHud(manager);
    }

    private void drawBackground() {
        batch.begin();
        batch.draw(backgroundTexture, 0f, 0f, camera.viewportWidth, camera.viewportHeight);
        batch.end();
    }

    private void drawBoard(Board board) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Tile tile : board.getTiles()) {
            Color base = tile.isPath() ? new Color(0.22f, 0.2f, 0.18f, 1f) : new Color(0.15f, 0.17f, 0.2f, 1f);
            shapeRenderer.setColor(base);
            shapeRenderer.rect(
                boardOrigin.x + tile.getX() * tileSize,
                boardOrigin.y + tile.getY() * tileSize,
                tileSize,
                tileSize
            );
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(new Color(0.2f, 0.25f, 0.3f, 1f));
        for (Tile tile : board.getTiles()) {
            shapeRenderer.rect(
                boardOrigin.x + tile.getX() * tileSize,
                boardOrigin.y + tile.getY() * tileSize,
                tileSize,
                tileSize
            );
        }
        shapeRenderer.end();
    }

    private void drawEntities(GameManager manager) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (TowerComponent tower : manager.getTowers()) {
            shapeRenderer.setColor(tower.getType().getColor());
            shapeRenderer.rect(
                boardOrigin.x + tower.getGridX() * tileSize + 8f,
                boardOrigin.y + tower.getGridY() * tileSize + 8f,
                tileSize - 16f,
                tileSize - 16f
            );
            if (tower == selectedTower) {
                shapeRenderer.setColor(new Color(0.95f, 0.9f, 0.4f, 1f));
                shapeRenderer.rect(
                    boardOrigin.x + tower.getGridX() * tileSize + 4f,
                    boardOrigin.y + tower.getGridY() * tileSize + 4f,
                    tileSize - 8f,
                    tileSize - 8f
                );
            }
        }

        for (Enemy enemy : manager.getEnemies()) {
            shapeRenderer.setColor(enemy.getType().getColor());
            shapeRenderer.circle(
                boardOrigin.x + enemy.getPosition().x,
                boardOrigin.y + enemy.getPosition().y,
                10f
            );

            float barWidth = 22f;
            float barHeight = 3f;
            float hpRatio = Math.max(0f, Math.min(1f, (float) enemy.getHp() / enemy.getMaxHp()));
            float barX = boardOrigin.x + enemy.getPosition().x - barWidth / 2f;
            float barY = boardOrigin.y + enemy.getPosition().y + 12f;
            shapeRenderer.setColor(new Color(0.2f, 0.2f, 0.2f, 1f));
            shapeRenderer.rect(barX, barY, barWidth, barHeight);
            shapeRenderer.setColor(new Color(0.2f, 0.85f, 0.2f, 1f));
            shapeRenderer.rect(barX, barY, barWidth * hpRatio, barHeight);
        }

        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (Projectile projectile : manager.getProjectiles()) {
            float alpha = projectile.getAlpha();
            shapeRenderer.setColor(new Color(0.9f, 0.8f, 0.3f, alpha));
            shapeRenderer.line(
                boardOrigin.x + projectile.getFrom().x,
                boardOrigin.y + projectile.getFrom().y,
                boardOrigin.x + projectile.getTo().x,
                boardOrigin.y + projectile.getTo().y
            );
        }
        TowerComponent rangeTower = selectedTower != null ? selectedTower : hoveredTower;
        if (rangeTower != null) {
            shapeRenderer.setColor(new Color(0.6f, 0.85f, 1f, 0.6f));
            float centerX = boardOrigin.x + (rangeTower.getGridX() + 0.5f) * tileSize;
            float centerY = boardOrigin.y + (rangeTower.getGridY() + 0.5f) * tileSize;
            shapeRenderer.circle(centerX, centerY, rangeTower.getRange());
        }
        shapeRenderer.end();
    }

    private void drawHud(GameManager manager) {
        drawHudPanel(manager);

        batch.begin();
        font.setColor(Color.WHITE);
        float statsX = hudPanelX + 16f;
        font.draw(batch, "Phase: " + manager.getPhase().getName(), statsX, 614f);
        font.draw(batch, "Energy: " + manager.getEnergy(), statsX, 590f);
        font.draw(batch, "Gold: " + manager.getGold(), statsX, 566f);
        font.draw(batch, "Base HP: " + manager.getBaseHealth(), statsX, 542f);
        font.draw(batch, "Wave: " + manager.getWavesCompleted() + " / " + manager.getMaxWaves(), statsX, 518f);
        if (manager.getNextCardDiscount() > 0) {
            font.draw(batch, "Discount: -" + manager.getNextCardDiscount(), statsX, 494f);
        }

        PendingTower pending = manager.getPendingTower();
        if (pending != null) {
            font.draw(batch, "Placing: " + clipText(pending.getName(), 24), statsX, 448f);
            font.draw(batch, "Click free tile", statsX, 430f);
        } else if (manager.getPendingTowerModifier() != null) {
            font.draw(batch, "Buff card active", statsX, 448f);
            font.draw(batch, "Click a tower", statsX, 430f);
        }
        font.draw(batch, "Hand (1-8)", statsX, handTitleY);

        float cardX = hudPanelX + 14f;
        float cardWidth = (hudPanelWidth - 28f - cardColumnGap) / 2f;
        int index = 0;
        for (Card card : manager.getHand().getCards()) {
            int col = index % 2;
            int row = index / 2;
            float x = cardX + col * (cardWidth + cardColumnGap);
            float y = cardsTopY - row * (cardHeight + cardGap);
            if (y < 68f) {
                break;
            }
            font.draw(batch, (index + 1) + ". " + clipText(card.getName(), 20), x + 6f, y + 46f);
            if (card instanceof BuildTowerCard) {
                font.draw(batch, "E:" + manager.getEffectiveEnergyCost(card) + " G:" + card.getCost(), x + 6f, y + 30f);
            } else {
                font.draw(batch, "E:" + manager.getEffectiveEnergyCost(card), x + 6f, y + 30f);
            }
            font.draw(batch, clipText(card.getDescription(), 30), x + 6f, y + 16f);
            index++;
        }

        if (manager.getPhase() instanceof GameOverPhase) {
            String result = ((GameOverPhase) manager.getPhase()).isVictory() ? "VICTORY" : "DEFEAT";
            font.draw(batch, result, 360f, 620f);
            font.draw(batch, "R: Restart", 350f, 594f);
            font.draw(batch, "ESC: Menu", 350f, 576f);
        }

        TowerComponent infoTower = selectedTower != null ? selectedTower : hoveredTower;
        if (infoTower != null) {
            float infoX = Math.min(mouseWorld.x + 12f, hudPanelX - 220f);
            float infoY = Math.min(mouseWorld.y + 12f, 620f);
            font.draw(batch, "Tower Info:", infoX, infoY);
            infoY -= 18f;
            font.draw(batch, infoTower.getName() + " [" + infoTower.getType().name() + "]", infoX, infoY);
            infoY -= 18f;
            font.draw(batch, "DMG: " + infoTower.getDamage(), infoX, infoY);
            infoY -= 18f;
            font.draw(batch, "RNG: " + (int) infoTower.getRange(), infoX, infoY);
            infoY -= 18f;
            font.draw(batch, "CD: " + String.format("%.2f", infoTower.getCooldown()), infoX, infoY);
            infoY -= 18f;
            font.draw(batch, "Lvl: " + infoTower.getLevel() + " | Upgrade: " + manager.getUpgradeCost(infoTower) + "g (U)", infoX, infoY);
            infoY -= 18f;
            Buffs buffs = collectBuffs(infoTower);
            if (buffs.damageBonus > 0 || buffs.rangeBonus > 0f || buffs.cooldownReduction > 0f) {
                font.draw(batch, "Buffs: +" + buffs.damageBonus + " dmg, +" + (int) buffs.rangeBonus + " rng, -" + String.format("%.2f", buffs.cooldownReduction) + " cd", infoX, infoY);
            }
        }

        font.draw(batch, "[1-8] Play card | SPACE: Start Wave | ESC: Back to Menu", 40f, 20f);
        batch.end();
    }

    private void drawPlacementPreview(GameManager manager) {
        PendingTower pending = manager.getPendingTower();
        if (pending == null || hoverGridX < 0 || hoverGridY < 0) {
            return;
        }

        Board board = manager.getBoard();
        if (hoverGridX >= board.getWidth() || hoverGridY >= board.getHeight()) {
            return;
        }

        boolean validTile = board.isBuildable(hoverGridX, hoverGridY) && !isTowerOccupied(manager, hoverGridX, hoverGridY);
        float tileX = boardOrigin.x + hoverGridX * tileSize;
        float tileY = boardOrigin.y + hoverGridY * tileSize;
        float centerX = tileX + tileSize * 0.5f;
        float centerY = tileY + tileSize * 0.5f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (validTile) {
            shapeRenderer.setColor(new Color(0.2f, 0.85f, 0.4f, 0.35f));
        } else {
            shapeRenderer.setColor(new Color(0.9f, 0.25f, 0.25f, 0.35f));
        }
        shapeRenderer.rect(tileX + 2f, tileY + 2f, tileSize - 4f, tileSize - 4f);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        if (validTile) {
            shapeRenderer.setColor(new Color(0.4f, 0.95f, 0.55f, 0.8f));
        } else {
            shapeRenderer.setColor(new Color(0.95f, 0.4f, 0.4f, 0.8f));
        }
        shapeRenderer.rect(tileX + 1f, tileY + 1f, tileSize - 2f, tileSize - 2f);
        shapeRenderer.circle(centerX, centerY, pending.getRange());
        shapeRenderer.end();
    }

    private void drawHudPanel(GameManager manager) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.04f, 0.07f, 0.12f, 0.98f));
        shapeRenderer.rect(hudPanelX, hudPanelY, hudPanelWidth, hudPanelHeight);

        // Visible pixel pattern for HUD section separation.
        shapeRenderer.setColor(new Color(0.13f, 0.2f, 0.3f, 0.55f));
        for (float x = hudPanelX + 4f; x < hudPanelX + hudPanelWidth - 4f; x += 10f) {
            for (float y = hudPanelY + 4f; y < hudPanelY + hudPanelHeight - 4f; y += 10f) {
                int parity = (((int) x / 10) + ((int) y / 10)) % 2;
                if (parity == 0) {
                    shapeRenderer.rect(x, y, 3f, 3f);
                }
            }
        }

        shapeRenderer.setColor(new Color(0.14f, 0.17f, 0.23f, 1f));
        shapeRenderer.rect(hudPanelX + 10f, statsBoxY, hudPanelWidth - 20f, statsBoxHeight);
        shapeRenderer.setColor(new Color(0.16f, 0.22f, 0.3f, 1f));
        shapeRenderer.rect(hudPanelX + 10f, handTitleY - 22f, hudPanelWidth - 20f, 24f);
        shapeRenderer.setColor(new Color(0.07f, 0.11f, 0.18f, 0.9f));
        shapeRenderer.rect(hudPanelX + 10f, hudPanelY + 8f, hudPanelWidth - 20f, handTitleY - hudPanelY - 34f);
        shapeRenderer.setColor(new Color(0.11f, 0.18f, 0.27f, 0.45f));
        shapeRenderer.rect(hudPanelX + 10f, hudPanelY + 8f, hudPanelWidth - 20f, 38f);

        float cardX = hudPanelX + 14f;
        float cardWidth = (hudPanelWidth - 28f - cardColumnGap) / 2f;
        int index = 0;
        for (Card card : manager.getHand().getCards()) {
            int col = index % 2;
            int row = index / 2;
            float x = cardX + col * (cardWidth + cardColumnGap);
            float y = cardsTopY - row * (cardHeight + cardGap);
            if (y < 68f) {
                break;
            }
            if (manager.canPlayCard(card)) {
                shapeRenderer.setColor(new Color(0.18f, 0.24f, 0.22f, 1f));
            } else {
                shapeRenderer.setColor(new Color(0.19f, 0.16f, 0.17f, 1f));
            }
            shapeRenderer.rect(x, y, cardWidth, cardHeight);
            index++;
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(new Color(0.3f, 0.37f, 0.46f, 1f));
        shapeRenderer.rect(hudPanelX, hudPanelY, hudPanelWidth, hudPanelHeight);
        shapeRenderer.rect(hudPanelX + 10f, statsBoxY, hudPanelWidth - 20f, statsBoxHeight);
        shapeRenderer.line(hudPanelX + 12f, handTitleY - 18f, hudPanelX + hudPanelWidth - 12f, handTitleY - 18f);
        float dividerX = hudPanelX - 20f;
        shapeRenderer.line(dividerX, 20f, dividerX, 680f);

        float cardXLine = hudPanelX + 14f;
        float cardWidthLine = (hudPanelWidth - 28f - cardColumnGap) / 2f;
        int indexLine = 0;
        for (Card ignored : manager.getHand().getCards()) {
            int col = indexLine % 2;
            int row = indexLine / 2;
            float x = cardXLine + col * (cardWidthLine + cardColumnGap);
            float y = cardsTopY - row * (cardHeight + cardGap);
            if (y < 68f) {
                break;
            }
            shapeRenderer.rect(x, y, cardWidthLine, cardHeight);
            indexLine++;
        }
        shapeRenderer.end();
    }

    private String clipText(String text, int maxChars) {
        if (text == null || text.length() <= maxChars) {
            return text;
        }
        return text.substring(0, maxChars - 3) + "...";
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
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
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
        backgroundTexture.dispose();
    }

    private Texture createPixelBackground() {
        int pxW = 300;
        int pxH = 175;
        Pixmap pixmap = new Pixmap(pxW, pxH, Pixmap.Format.RGBA8888);

        for (int y = 0; y < pxH; y++) {
            float t = (float) y / (float) pxH;
            float r = 0.03f + 0.05f * t;
            float g = 0.05f + 0.06f * t;
            float b = 0.1f + 0.09f * t;
            pixmap.setColor(r, g, b, 1f);
            pixmap.drawLine(0, y, pxW - 1, y);
        }

        pixmap.setColor(0.07f, 0.11f, 0.18f, 1f);
        for (int x = 0; x < pxW; x++) {
            int h = 22 + (int) (10 * Math.sin(x * 0.12f)) + (int) (7 * Math.sin(x * 0.2f + 1.8f));
            pixmap.drawLine(x, 0, x, h);
        }

        pixmap.setColor(0.09f, 0.14f, 0.21f, 1f);
        for (int x = 0; x < pxW; x += 8) {
            for (int y = 0; y < pxH; y += 8) {
                if (((x + y) / 8) % 2 == 0) {
                    pixmap.fillRectangle(x, y, 1, 1);
                }
            }
        }

        pixmap.setColor(0.75f, 0.85f, 1f, 0.8f);
        for (int i = 0; i < 90; i++) {
            int sx = 2 + (i * 23) % (pxW - 4);
            int sy = 62 + (i * 31) % (pxH - 66);
            pixmap.fillRectangle(sx, sy, 1, 1);
        }

        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        pixmap.dispose();
        return texture;
    }

    private class GameInputProcessor extends InputAdapter {
        @Override
        public boolean keyDown(int keycode) {
            GameManager manager = GameManager.get();
            if (keycode == Input.Keys.ESCAPE) {
                game.showMenu();
                return true;
            }
            if (manager.getPhase() instanceof GameOverPhase && keycode == Input.Keys.R) {
                game.startGame();
                return true;
            }
            if (keycode == Input.Keys.SPACE) {
                manager.requestWaveStart();
                return true;
            }
            if (keycode == Input.Keys.U) {
                if (selectedTower != null) {
                    TowerComponent upgraded = manager.upgradeTower(selectedTower);
                    if (upgraded != null) {
                        selectedTower = upgraded;
                    }
                }
                return true;
            }
            if (keycode >= Input.Keys.NUM_1 && keycode <= Input.Keys.NUM_8) {
                int index = keycode - Input.Keys.NUM_1;
                manager.playCard(index);
                return true;
            }
            return false;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            GameManager manager = GameManager.get();
            com.badlogic.gdx.math.Vector3 world3 = camera.unproject(new com.badlogic.gdx.math.Vector3(screenX, screenY, 0f));
            Vector2 world = new Vector2(world3.x, world3.y);
            int gridX = (int) ((world.x - boardOrigin.x) / tileSize);
            int gridY = (int) ((world.y - boardOrigin.y) / tileSize);
            TowerComponent tower = findTowerAt(gridX, gridY);
            if (manager.getPendingTowerModifier() != null) {
                if (tower != null) {
                    TowerComponent buffedTower = manager.tryApplyPendingTowerModifier(tower);
                    if (buffedTower != null) {
                        selectedTower = buffedTower;
                        return true;
                    }
                }
                return true;
            }
            if (tower != null) {
                selectedTower = tower;
                return true;
            }
            selectedTower = null;
            return manager.tryPlacePendingTower(gridX, gridY);
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY) {
            com.badlogic.gdx.math.Vector3 world3 = camera.unproject(new com.badlogic.gdx.math.Vector3(screenX, screenY, 0f));
            Vector2 world = new Vector2(world3.x, world3.y);
            int gridX = (int) ((world.x - boardOrigin.x) / tileSize);
            int gridY = (int) ((world.y - boardOrigin.y) / tileSize);
            hoveredTower = findTowerAt(gridX, gridY);
            mouseWorld.set(world.x, world.y);
            hoverGridX = gridX;
            hoverGridY = gridY;
            return false;
        }
    }

    private TowerComponent findTowerAt(int gridX, int gridY) {
        for (TowerComponent tower : GameManager.get().getTowers()) {
            if (tower.getGridX() == gridX && tower.getGridY() == gridY) {
                return tower;
            }
        }
        return null;
    }

    private boolean isTowerOccupied(GameManager manager, int gridX, int gridY) {
        for (TowerComponent tower : manager.getTowers()) {
            if (tower.getGridX() == gridX && tower.getGridY() == gridY) {
                return true;
            }
        }
        return false;
    }

    private Buffs collectBuffs(TowerComponent tower) {
        int damageBonus = 0;
        float rangeBonus = 0f;
        float cooldownReduction = 0f;
        TowerComponent current = tower;
        while (current instanceof TowerDecorator) {
            if (current instanceof DamageBuffDecorator) {
                damageBonus += ((DamageBuffDecorator) current).getBonusDamage();
            } else if (current instanceof RangeBuffDecorator) {
                rangeBonus += ((RangeBuffDecorator) current).getBonusRange();
            } else if (current instanceof UpgradeDecorator) {
                damageBonus += ((UpgradeDecorator) current).getBonusDamage();
                rangeBonus += ((UpgradeDecorator) current).getBonusRange();
                cooldownReduction += ((UpgradeDecorator) current).getCooldownReduction();
            }
            current = ((TowerDecorator) current).getInner();
        }
        return new Buffs(damageBonus, rangeBonus, cooldownReduction);
    }

    private static class Buffs {
        final int damageBonus;
        final float rangeBonus;
        final float cooldownReduction;

        Buffs(int damageBonus, float rangeBonus, float cooldownReduction) {
            this.damageBonus = damageBonus;
            this.rangeBonus = rangeBonus;
            this.cooldownReduction = cooldownReduction;
        }
    }
}

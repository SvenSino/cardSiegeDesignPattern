package td.core.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import td.core.MainGame;

public class MenuScreen implements Screen {
    private final MainGame game;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final GlyphLayout glyphLayout = new GlyphLayout();
    private final Texture pixelBg;
    private float panelX;
    private float panelY;
    private float panelW = 560f;
    private float panelH = 360f;

    public MenuScreen(MainGame game) {
        this.game = game;
        camera.setToOrtho(false, 1200f, 700f);
        recalcLayout(1200f, 700f);
        pixelBg = createPixelBackground();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                    game.startGame();
                    return true;
                }
                if (keycode == Input.Keys.ESCAPE) {
                    Gdx.app.exit();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.07f, 0.09f, 0.13f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.draw(pixelBg, 0f, 0f, camera.viewportWidth, camera.viewportHeight);
        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.1f, 0.14f, 0.2f, 0.86f));
        shapeRenderer.rect(panelX, panelY, panelW, panelH);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(new Color(0.35f, 0.45f, 0.58f, 1f));
        shapeRenderer.rect(panelX, panelY, panelW, panelH);
        shapeRenderer.end();

        batch.begin();
        font.setColor(Color.WHITE);
        drawCentered("PATHWEAVER: CARD SIEGE", panelY + 270f);
        drawCentered("ENTER or SPACE: Start", panelY + 180f);
        drawCentered("ESC: Exit", panelY + 150f);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
        recalcLayout(width, height);
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
        pixelBg.dispose();
    }

    private void recalcLayout(float width, float height) {
        panelX = (width - panelW) * 0.5f;
        panelY = (height - panelH) * 0.5f;
    }

    private void drawCentered(String text, float y) {
        glyphLayout.setText(font, text);
        float x = panelX + (panelW - glyphLayout.width) * 0.5f;
        font.draw(batch, glyphLayout, x, y);
    }

    private Texture createPixelBackground() {
        int pxW = 300;
        int pxH = 175;
        Pixmap pixmap = new Pixmap(pxW, pxH, Pixmap.Format.RGBA8888);

        // Night sky gradient blocks
        for (int y = 0; y < pxH; y++) {
            float t = (float) y / (float) pxH;
            float r = 0.04f + 0.04f * t;
            float g = 0.07f + 0.05f * t;
            float b = 0.14f + 0.09f * t;
            pixmap.setColor(r, g, b, 1f);
            pixmap.drawLine(0, y, pxW - 1, y);
        }

        // Pixel stars
        pixmap.setColor(0.82f, 0.9f, 1f, 1f);
        for (int i = 0; i < 120; i++) {
            int x = 3 + (i * 19) % (pxW - 6);
            int y = 60 + (i * 37) % (pxH - 65);
            pixmap.fillRectangle(x, y, 1, 1);
        }

        // Stylized mountain silhouettes
        pixmap.setColor(0.08f, 0.12f, 0.19f, 1f);
        for (int x = 0; x < pxW; x++) {
            int h = 20 + (int) (12 * Math.sin(x * 0.08f)) + (int) (8 * Math.sin(x * 0.17f + 1.3f));
            pixmap.drawLine(x, 0, x, h);
        }

        // Foreground towers/castle blocks
        pixmap.setColor(0.12f, 0.17f, 0.26f, 1f);
        pixmap.fillRectangle(26, 8, 28, 36);
        pixmap.fillRectangle(58, 8, 16, 52);
        pixmap.fillRectangle(98, 8, 34, 42);
        pixmap.fillRectangle(142, 8, 22, 64);
        pixmap.fillRectangle(170, 8, 30, 40);
        pixmap.fillRectangle(208, 8, 18, 56);
        pixmap.fillRectangle(230, 8, 42, 34);

        // Lit windows accents
        pixmap.setColor(0.92f, 0.78f, 0.35f, 1f);
        for (int y = 14; y < 64; y += 10) {
            pixmap.fillRectangle(62, y, 2, 3);
            pixmap.fillRectangle(146, y, 2, 3);
            pixmap.fillRectangle(212, y, 2, 3);
        }

        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        pixmap.dispose();
        return texture;
    }
}

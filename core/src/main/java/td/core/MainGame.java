package td.core;

import td.core.ui.GameScreen;
import td.core.ui.MenuScreen;
import com.badlogic.gdx.Game;

public class MainGame extends Game {
    @Override
    public void create() {
        showMenu();
    }

    public void startGame() {
        setScreen(new GameScreen(this));
    }

    public void showMenu() {
        setScreen(new MenuScreen(this));
    }
}

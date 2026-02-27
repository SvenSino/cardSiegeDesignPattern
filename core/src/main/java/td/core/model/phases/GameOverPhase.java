package td.core.model.phases;

import td.core.model.game.GameManager;
public class GameOverPhase implements GamePhase {
    private final boolean victory;

    public GameOverPhase(boolean victory) {
        this.victory = victory;
    }

    @Override
    public String getName() {
        return victory ? "Victory" : "Defeat";
    }

    @Override
    public void enter(GameManager manager) {
    }

    @Override
    public void update(GameManager manager, float delta) {
    }

    public boolean isVictory() {
        return victory;
    }
}
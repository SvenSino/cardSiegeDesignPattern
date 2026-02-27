package td.core.model.phases;

import td.core.model.game.GameManager;
public class PlayPhase implements GamePhase {
    @Override
    public String getName() {
        return "Play";
    }

    @Override
    public void enter(GameManager manager) {
    }

    @Override
    public void update(GameManager manager, float delta) {
    }
}
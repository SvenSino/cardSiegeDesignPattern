package td.core.model.phases;

import td.core.model.game.GameManager;
public class ResolvePhase implements GamePhase {
    private float timer;

    @Override
    public String getName() {
        return "Resolve";
    }

    @Override
    public void enter(GameManager manager) {
        timer = 0f;
    }

    @Override
    public void update(GameManager manager, float delta) {
        timer += delta;
        if (timer > 0.8f) {
            manager.switchPhase(new DrawPhase());
        }
    }
}
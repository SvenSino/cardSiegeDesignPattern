package td.core.model.phases;

import td.core.model.game.GameManager;
public class DrawPhase implements GamePhase {
    @Override
    public String getName() {
        return "Draw";
    }

    @Override
    public void enter(GameManager manager) {
        manager.drawHand(3);
        manager.resetEnergy(3);
        manager.switchPhase(new PlayPhase());
    }

    @Override
    public void update(GameManager manager, float delta) {
    }
}
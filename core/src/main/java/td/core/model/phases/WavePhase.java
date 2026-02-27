package td.core.model.phases;

import td.core.model.game.GameManager;
public class WavePhase implements GamePhase {
    @Override
    public String getName() {
        return "Wave";
    }

    @Override
    public void enter(GameManager manager) {
        manager.beginWave();
    }

    @Override
    public void update(GameManager manager, float delta) {
        manager.endWaveIfComplete();
    }
}
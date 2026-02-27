package td.core.model.events;

import lombok.Getter;

@Getter
public class GameEvent {
    public static final GameEvent WaveStarted = new GameEvent("WaveStarted");
    public static final GameEvent EnemyDefeated = new GameEvent("EnemyDefeated");

    private final String name;

    private GameEvent(String name) {
        this.name = name;
    }

    public static GameEvent phaseChanged(String phaseName) {
        return new GameEvent("PhaseChanged:" + phaseName);
    }

    public static GameEvent cardPlayed(String cardName) {
        return new GameEvent("CardPlayed:" + cardName);
    }

    public static GameEvent towerBuilt(int x, int y) {
        return new GameEvent("TowerBuilt:" + x + "," + y);
    }

    public static GameEvent baseDamaged(int remainingHealth) {
        return new GameEvent("BaseDamaged:" + remainingHealth);
    }

}
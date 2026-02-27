package td.core.model.phases;

import td.core.model.game.GameManager;
public interface GamePhase {
    String getName();
    void enter(GameManager manager);
    void update(GameManager manager, float delta);
}
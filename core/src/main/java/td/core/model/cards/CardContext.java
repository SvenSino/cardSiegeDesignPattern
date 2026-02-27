package td.core.model.cards;

import td.core.model.game.GameManager;
import lombok.Getter;

@Getter
public class CardContext {
    private final GameManager manager;

    public CardContext(GameManager manager) {
        this.manager = manager;
    }
}
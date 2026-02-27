package td.core.model.cards;

import java.util.List;

public interface CardFactory {
    List<Card> createDeck();
}
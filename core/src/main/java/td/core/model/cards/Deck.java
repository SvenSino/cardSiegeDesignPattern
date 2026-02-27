package td.core.model.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private final CardFactory factory;
    private final List<Card> cards = new ArrayList<>();

    public Deck(CardFactory factory) {
        this.factory = factory;
        refill();
    }

    public Card draw() {
        if (cards.isEmpty()) {
            refill();
        }
        return cards.removeFirst();
    }

    public void putBottom(Card card) {
        if (card != null) {
            cards.addLast(card);
        }
    }

    private void refill() {
        cards.clear();
        for (Card prototype : factory.createDeck()) {
            cards.add(prototype.copy());
            cards.add(prototype.copy());
        }
        Collections.shuffle(cards);
    }
}

package td.core.model.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Hand {
    private static final int MAX_HAND_SIZE = 8;
    private final List<Card> cards = new ArrayList<>();

    public void add(Card card) {
        if (card != null && cards.size() < MAX_HAND_SIZE) {
            cards.add(card);
        }
    }

    public void addToFront(Card card) {
        if (card != null && cards.size() < MAX_HAND_SIZE) {
            cards.addFirst(card);
        }
    }

    public Card removeAt(int index) {
        if (index < 0 || index >= cards.size()) {
            return null;
        }
        return cards.remove(index);
    }

    public List<Card> getCards() {
        return Collections.unmodifiableList(cards);
    }
}

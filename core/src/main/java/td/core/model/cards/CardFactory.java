package td.core.model.cards;

import java.util.List;

/**
 * Factory-Method-Interface für die Deck-Erzeugung.
 * <p>
 * Das {@code Deck} kennt ausschließlich dieses Interface und bleibt dadurch
 * unabhängig von konkreten Kartentypen. Eine neue Implementierung genügt,
 * um den gesamten Kartensatz auszutauschen (z.B. für ein Tutorial-Deck).
 * </p>
 *
 * @see td.core.model.cards.StrategyCardFactory
 */
public interface CardFactory {

    /**
     * Erzeugt eine vollständige Liste konfigurierter Karten-Prototypen.
     * Das {@code Deck} klont jeden Prototyp, bevor es ihn verwendet.
     *
     * @return Liste der Prototyp-Karten für ein neues Deck
     */
    List<Card> createDeck();
}
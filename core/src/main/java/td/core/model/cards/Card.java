package td.core.model.cards;

import lombok.Getter;

/**
 * Abstrakte Basisklasse aller Spielkarten (Command-Pattern).
 * <p>
 * Jede konkrete Karte ist ein ConcreteCommand: Sie kennt ihren eigenen Parameter
 * (z.B. Schadenswert, Dauer) und delegiert die Ausführung über {@link #execute}
 * an den {@code GameManager} als Receiver.
 * </p>
 */
@Getter
public abstract class Card {
    private final String name;
    private final int cost;
    private final String description;

    protected Card(String name, int cost, String description) {
        this.name = name;
        this.cost = cost;
        this.description = description;
    }

    /**
     * Führt die Kartenaktion aus.
     *
     * @param context Zugriff auf den {@code GameManager} als Receiver
     */
    public abstract void execute(CardContext context);

    /**
     * Erstellt eine unabhängige Kopie dieser Karte (Prototype-Pattern).
     *
     * @return neue Instanz mit identischen Parametern
     */
    public abstract Card copy();
}
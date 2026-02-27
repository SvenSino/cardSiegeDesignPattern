package td.core.model.cards;

import lombok.Getter;

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

    public abstract void execute(CardContext context);

    public abstract Card copy();
}
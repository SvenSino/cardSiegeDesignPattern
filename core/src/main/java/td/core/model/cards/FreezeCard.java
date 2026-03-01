package td.core.model.cards;

public class FreezeCard extends Card {
    private final float duration;
    private final float slowFactor;

    public FreezeCard(String name, int cost, float duration, float slowFactor) {
        super(name, cost, "Slow enemies for " + duration + "s");
        this.duration = duration;
        this.slowFactor = slowFactor;
    }

    @Override
    public void execute(CardContext context) {
        context.getManager().slowAllEnemies(duration, slowFactor);
    }

    @Override
    public Card copy() {
        return new FreezeCard(getName(), getCost(), duration, slowFactor);
    }
}
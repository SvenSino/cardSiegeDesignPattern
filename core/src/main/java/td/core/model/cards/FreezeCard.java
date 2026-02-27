package td.core.model.cards;

import td.core.model.enemies.Enemy;
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
        for (Enemy enemy : context.getManager().getEnemies()) {
            enemy.applySlow(duration, slowFactor);
        }
    }

    @Override
    public Card copy() {
        return new FreezeCard(getName(), getCost(), duration, slowFactor);
    }
}
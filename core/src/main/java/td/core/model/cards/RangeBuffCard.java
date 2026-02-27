package td.core.model.cards;

import td.core.model.towers.RangeBuffDecorator;
public class RangeBuffCard extends Card {
    private final float bonusRange;

    public RangeBuffCard(String name, int cost, float bonusRange) {
        super(name, cost, "Buff one tower: +" + (int) bonusRange + " range");
        this.bonusRange = bonusRange;
    }

    @Override
    public void execute(CardContext context) {
        context.getManager().queueTowerModifier(tower -> new RangeBuffDecorator(tower, bonusRange));
    }

    @Override
    public Card copy() {
        return new RangeBuffCard(getName(), getCost(), bonusRange);
    }
}

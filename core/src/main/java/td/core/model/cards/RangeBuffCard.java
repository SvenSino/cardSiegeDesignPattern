package td.core.model.cards;

public class RangeBuffCard extends Card {
    private final float bonusRange;

    public RangeBuffCard(String name, int cost, float bonusRange) {
        super(name, cost, "Buff one tower: +" + (int) bonusRange + " range");
        this.bonusRange = bonusRange;
    }

    @Override
    public void execute(CardContext context) {
        context.getManager().queueRangeBuff(bonusRange);
    }

    @Override
    public Card copy() {
        return new RangeBuffCard(getName(), getCost(), bonusRange);
    }
}

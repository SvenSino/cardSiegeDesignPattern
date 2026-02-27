package td.core.model.cards;

public class GoldCard extends Card {
    private final int goldGain;

    public GoldCard(String name, int cost, int goldGain) {
        super(name, cost, "Gain +" + goldGain + " gold");
        this.goldGain = goldGain;
    }

    @Override
    public void execute(CardContext context) {
        context.getManager().addGold(goldGain);
    }

    @Override
    public Card copy() {
        return new GoldCard(getName(), getCost(), goldGain);
    }
}
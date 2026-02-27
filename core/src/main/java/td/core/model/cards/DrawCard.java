package td.core.model.cards;

public class DrawCard extends Card {
    private final int drawCount;

    public DrawCard(String name, int cost, int drawCount) {
        super(name, cost, "Draw " + drawCount + " cards");
        this.drawCount = drawCount;
    }

    @Override
    public void execute(CardContext context) {
        context.getManager().drawHand(drawCount);
    }

    @Override
    public Card copy() {
        return new DrawCard(getName(), getCost(), drawCount);
    }
}
package td.core.model.cards;

public class  DiscountCard extends Card {
    private final int discount;

    public DiscountCard(String name, int cost, int discount) {
        super(name, cost, "Next card costs -" + discount);
        this.discount = discount;
    }

    @Override
    public void execute(CardContext context) {
        context.getManager().addNextCardDiscount(discount);
    }

    @Override
    public Card copy() {
        return new DiscountCard(getName(), getCost(), discount);
    }
}
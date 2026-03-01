package td.core.model.cards;

public class DamageBuffCard extends Card {
    private final int bonusDamage;

    public DamageBuffCard(String name, int cost, int bonusDamage) {
        super(name, cost, "All towers +" + bonusDamage + " damage");
        this.bonusDamage = bonusDamage;
    }

    @Override
    public void execute(CardContext context) {
        context.getManager().applyDamageBuff(bonusDamage);
    }

    @Override
    public Card copy() {
        return new DamageBuffCard(getName(), getCost(), bonusDamage);
    }
}

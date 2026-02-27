package td.core.model.cards;

import td.core.model.towers.DamageBuffDecorator;
public class DamageBuffCard extends Card {
    private final int bonusDamage;

    public DamageBuffCard(String name, int cost, int bonusDamage) {
        super(name, cost, "All towers +" + bonusDamage + " damage");
        this.bonusDamage = bonusDamage;
    }

    @Override
    public void execute(CardContext context) {
        context.getManager().applyTowerModifier(tower -> new DamageBuffDecorator(tower, bonusDamage));
    }

    @Override
    public Card copy() {
        return new DamageBuffCard(getName(), getCost(), bonusDamage);
    }
}

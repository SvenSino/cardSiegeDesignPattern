package td.core.model.cards;

public class EnergyCard extends Card {
    private final int energyGain;

    public EnergyCard(String name, int cost, int energyGain) {
        super(name, cost, "Gain +" + energyGain + " energy");
        this.energyGain = energyGain;
    }

    @Override
    public void execute(CardContext context) {
        context.getManager().addEnergy(energyGain);
    }

    @Override
    public Card copy() {
        return new EnergyCard(getName(), getCost(), energyGain);
    }
}
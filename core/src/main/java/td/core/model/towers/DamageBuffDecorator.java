package td.core.model.towers;

import lombok.Getter;

public class DamageBuffDecorator extends TowerDecorator {
    @Getter
    private final int bonusDamage;

    public DamageBuffDecorator(TowerComponent inner, int bonusDamage) {
        super(inner);
        this.bonusDamage = bonusDamage;
    }

    @Override
    public int getDamage() {
        return inner.getDamage() + bonusDamage;
    }

}
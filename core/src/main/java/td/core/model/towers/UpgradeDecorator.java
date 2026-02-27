package td.core.model.towers;

import lombok.Getter;

public class UpgradeDecorator extends TowerDecorator {
    @Getter
    private final int bonusDamage;
    @Getter
    private final float bonusRange;
    @Getter
    private final float cooldownReduction;

    public UpgradeDecorator(TowerComponent inner, int bonusDamage, float bonusRange, float cooldownReduction) {
        super(inner);
        this.bonusDamage = bonusDamage;
        this.bonusRange = bonusRange;
        this.cooldownReduction = cooldownReduction;
    }

    @Override
    public int getDamage() {
        return inner.getDamage() + bonusDamage;
    }

    @Override
    public float getRange() {
        return inner.getRange() + bonusRange;
    }

    @Override
    public float getCooldown() {
        return Math.max(0.2f, inner.getCooldown() - cooldownReduction);
    }

    @Override
    public int getLevel() {
        return inner.getLevel() + 1;
    }

}
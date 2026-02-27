package td.core.model.towers;

import lombok.Getter;

public class RangeBuffDecorator extends TowerDecorator {
    @Getter
    private final float bonusRange;

    public RangeBuffDecorator(TowerComponent inner, float bonusRange) {
        super(inner);
        this.bonusRange = bonusRange;
    }

    @Override
    public float getRange() {
        return inner.getRange() + bonusRange;
    }

}
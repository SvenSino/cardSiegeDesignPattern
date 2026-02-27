package td.core.model.towers;

import lombok.Getter;

@Getter
public class PendingTower {
    private final String name;
    private final TowerType type;
    private final int cost;
    private final int baseDamage;
    private final float range;
    private final float cooldown;
    private final TargetingStrategy targeting;
    private final AttackStrategy attackStrategy;

    public PendingTower(String name, TowerType type, int cost, int baseDamage, float range, float cooldown, TargetingStrategy targeting, AttackStrategy attackStrategy) {
        this.name = name;
        this.type = type;
        this.cost = cost;
        this.baseDamage = baseDamage;
        this.range = range;
        this.cooldown = cooldown;
        this.targeting = targeting;
        this.attackStrategy = attackStrategy;
    }

    public TowerComponent build(int gridX, int gridY) {
        return new BaseTower(name, type, gridX, gridY, baseDamage, range, cooldown, cost, targeting, attackStrategy);
    }

}

package td.core.model.cards;

import td.core.model.towers.PendingTower;
import td.core.model.towers.AttackStrategy;
import td.core.model.towers.TargetingStrategy;
import td.core.model.towers.TowerType;
public class BuildTowerCard extends Card {
    private final int damage;
    private final float range;
    private final float cooldown;
    private final TargetingStrategy targeting;
    private final AttackStrategy attackStrategy;
    private final TowerType type;

    public BuildTowerCard(String name, int cost, int damage, float range, float cooldown, TargetingStrategy targeting, AttackStrategy attackStrategy, TowerType type) {
        super(name, cost, "Build tower (DMG " + damage + ", RNG " + (int) range + ")");
        this.damage = damage;
        this.range = range;
        this.cooldown = cooldown;
        this.targeting = targeting;
        this.attackStrategy = attackStrategy;
        this.type = type;
    }

    @Override
    public void execute(CardContext context) {
        PendingTower pending = new PendingTower(getName(), type, getCost(), damage, range, cooldown, targeting, attackStrategy);
        context.getManager().queueTowerPlacement(pending);
    }

    @Override
    public Card copy() {
        return new BuildTowerCard(getName(), getCost(), damage, range, cooldown, targeting, attackStrategy, type);
    }
}

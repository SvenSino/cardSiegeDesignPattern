package td.core.model.towers;

import td.core.model.enemies.EnemyComponent;

import java.util.ArrayList;
import java.util.List;

public class SingleTargetAttackStrategy implements AttackStrategy {
    @Override
    public EnemyComponent attack(TowerComponent tower) {
        List<EnemyComponent> inRange = new ArrayList<>(tower.getEnemiesInRange());
        if (inRange.isEmpty()) {
            return null;
        }
        EnemyComponent target = tower.getTargetingStrategy().selectTarget(tower, inRange);
        if (target == null) {
            return null;
        }
        target.takeDamage(tower.getDamage());
        return target;
    }
}

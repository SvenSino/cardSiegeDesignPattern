package td.core.model.towers;

import td.core.model.enemies.Enemy;

import java.util.ArrayList;
import java.util.List;

public class SingleTargetAttackStrategy implements AttackStrategy {
    @Override
    public Enemy attack(TowerComponent tower) {
        List<Enemy> inRange = new ArrayList<>(tower.getEnemiesInRange());
        if (inRange.isEmpty()) {
            return null;
        }
        Enemy target = tower.getTargetingStrategy().selectTarget(tower, inRange);
        if (target == null) {
            return null;
        }
        target.takeDamage(tower.getDamage());
        return target;
    }
}

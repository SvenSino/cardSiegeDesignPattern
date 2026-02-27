package td.core.model.towers;

import td.core.model.enemies.Enemy;

import java.util.ArrayList;
import java.util.List;

public class SplashAttackStrategy implements AttackStrategy {
    private final float splashRadius;
    private final float splashFactor;

    public SplashAttackStrategy(float splashRadius, float splashFactor) {
        this.splashRadius = splashRadius;
        this.splashFactor = splashFactor;
    }

    @Override
    public Enemy attack(TowerComponent tower) {
        List<Enemy> inRange = new ArrayList<>(tower.getEnemiesInRange());
        if (inRange.isEmpty()) {
            return null;
        }

        Enemy primary = tower.getTargetingStrategy().selectTarget(tower, inRange);
        if (primary == null) {
            return null;
        }

        primary.takeDamage(tower.getDamage());
        int splashDamage = Math.max(1, Math.round(tower.getDamage() * splashFactor));
        float radiusSq = splashRadius * splashRadius;

        for (Enemy enemy : inRange) {
            if (enemy == primary) {
                continue;
            }
            float dx = enemy.getPosition().x - primary.getPosition().x;
            float dy = enemy.getPosition().y - primary.getPosition().y;
            if ((dx * dx + dy * dy) <= radiusSq) {
                enemy.takeDamage(splashDamage);
            }
        }
        return primary;
    }
}

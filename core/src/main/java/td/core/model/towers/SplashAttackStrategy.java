package td.core.model.towers;

import td.core.model.enemies.EnemyComponent;

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
    public EnemyComponent attack(TowerComponent tower) {
        List<EnemyComponent> inRange = new ArrayList<>(tower.getEnemiesInRange());
        if (inRange.isEmpty()) {
            return null;
        }

        EnemyComponent primary = tower.getTargetingStrategy().selectTarget(tower, inRange);
        if (primary == null) {
            return null;
        }

        primary.takeDamage(tower.getDamage());
        int splashDamage = Math.max(1, Math.round(tower.getDamage() * splashFactor));
        float radiusSq = splashRadius * splashRadius;

        for (EnemyComponent component : inRange) {
            if (component == primary) {
                continue;
            }
            float dx = component.getPosition().x - primary.getPosition().x;
            float dy = component.getPosition().y - primary.getPosition().y;
            if ((dx * dx + dy * dy) <= radiusSq) {
                component.takeDamage(splashDamage);
            }
        }
        return primary;
    }
}

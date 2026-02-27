package td.core.model.towers;

import td.core.model.enemies.Enemy;

public interface AttackStrategy {
    Enemy attack(TowerComponent tower);
}

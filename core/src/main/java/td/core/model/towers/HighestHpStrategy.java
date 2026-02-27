package td.core.model.towers;

import td.core.model.enemies.Enemy;
import java.util.Comparator;
import java.util.List;

public class HighestHpStrategy implements TargetingStrategy {
    @Override
    public Enemy selectTarget(TowerComponent tower, List<Enemy> enemies) {
        return enemies.stream()
            .filter(enemy -> enemy.isInRange(tower))
            .max(Comparator.comparingInt(Enemy::getHp))
            .orElse(null);
    }
}
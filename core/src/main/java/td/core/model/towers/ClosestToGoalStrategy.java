package td.core.model.towers;

import td.core.model.enemies.Enemy;
import java.util.Comparator;
import java.util.List;

public class ClosestToGoalStrategy implements TargetingStrategy {
    @Override
    public Enemy selectTarget(TowerComponent tower, List<Enemy> enemies) {
        return enemies.stream()
            .filter(enemy -> enemy.isInRange(tower))
            .max(Comparator.comparingInt(Enemy::getPathIndex))
            .orElse(null);
    }
}
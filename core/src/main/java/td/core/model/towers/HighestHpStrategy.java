package td.core.model.towers;

import td.core.model.enemies.EnemyComponent;
import java.util.Comparator;
import java.util.List;

public class HighestHpStrategy implements TargetingStrategy {
    @Override
    public EnemyComponent selectTarget(TowerComponent tower, List<EnemyComponent> enemies) {
        return enemies.stream()
            .filter(enemy -> enemy.isInRange(tower))
            .max(Comparator.comparingInt(EnemyComponent::getHp))
            .orElse(null);
    }
}

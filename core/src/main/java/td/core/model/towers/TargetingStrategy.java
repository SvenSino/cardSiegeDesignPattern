package td.core.model.towers;

import td.core.model.enemies.Enemy;
import java.util.List;

public interface TargetingStrategy {
    Enemy selectTarget(TowerComponent tower, List<Enemy> enemies);
}
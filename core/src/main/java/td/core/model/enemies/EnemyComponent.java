package td.core.model.enemies;

import com.badlogic.gdx.math.Vector2;
import td.core.model.towers.TowerComponent;
import java.util.List;

public interface EnemyComponent {
    void update(float delta);
    void takeDamage(int amount);
    boolean isDead();
    boolean hasReachedGoal();
    void applySlow(float duration, float factor);
    Vector2 getPosition();
    int getHp();
    int getMaxHp();
    int getPathIndex();
    EnemyType getType();
    boolean isInRange(TowerComponent tower);
    List<EnemyComponent> getLeaves();
    int getGoldValue();
}

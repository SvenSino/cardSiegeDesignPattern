package td.core.model.towers;

import td.core.model.enemies.Enemy;

import java.util.Set;

public interface TowerComponent {
    int getGridX();
    int getGridY();
    String getName();
    TowerType getType();
    int getLevel();
    int getDamage();
    float getRange();
    float getCooldown();
    int getCost();
    TargetingStrategy getTargetingStrategy();
    AttackStrategy getAttackStrategy();
    Set<Enemy> getEnemiesInRange();
    void onEnemyEnteredRange(Enemy enemy);
    void onEnemyExitedRange(Enemy enemy);

    boolean canFire();
    void resetCooldown();
    void tickCooldown(float delta);
    void update(float delta);
}

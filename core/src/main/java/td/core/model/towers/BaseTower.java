package td.core.model.towers;

import td.core.model.enemies.Enemy;
import lombok.Getter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
public class BaseTower implements TowerComponent {
    private final String name;
    private final TowerType type;
    private final int gridX;
    private final int gridY;
    private final int damage;
    private final float range;
    private final float cooldown;
    private final int cost;
    private final TargetingStrategy targetingStrategy;
    private final AttackStrategy attackStrategy;
    private final Set<Enemy> enemiesInRange = new LinkedHashSet<>();

    private float cooldownTimer;

    public BaseTower(String name, TowerType type, int gridX, int gridY, int damage, float range, float cooldown, int cost, TargetingStrategy targetingStrategy, AttackStrategy attackStrategy) {
        this.name = name;
        this.type = type;
        this.gridX = gridX;
        this.gridY = gridY;
        this.damage = damage;
        this.range = range;
        this.cooldown = cooldown;
        this.cost = cost;
        this.targetingStrategy = targetingStrategy;
        this.attackStrategy = attackStrategy;
    }

    @Override
    public int getLevel() {
        return 1;
    }

    @Override
    public boolean canFire() {
        return cooldownTimer <= 0f;
    }

    @Override
    public void resetCooldown() {
        cooldownTimer = cooldown;
    }

    @Override
    public void tickCooldown(float delta) {
        cooldownTimer -= delta;
    }

    @Override
    public void update(float delta) {
        // no-op for base tower
    }

    @Override
    public void onEnemyEnteredRange(Enemy enemy) {
        enemiesInRange.add(enemy);
    }

    @Override
    public void onEnemyExitedRange(Enemy enemy) {
        enemiesInRange.remove(enemy);
    }
}

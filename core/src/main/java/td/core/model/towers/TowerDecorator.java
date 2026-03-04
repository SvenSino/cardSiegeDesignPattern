package td.core.model.towers;

import td.core.model.enemies.EnemyComponent;
import lombok.Getter;

import java.util.Set;

public abstract class TowerDecorator implements TowerComponent {
    @Getter
    protected final TowerComponent inner;

    protected TowerDecorator(TowerComponent inner) {
        this.inner = inner;
    }

    @Override
    public int getGridX() {
        return inner.getGridX();
    }

    @Override
    public int getGridY() {
        return inner.getGridY();
    }

    @Override
    public String getName() {
        return inner.getName();
    }

    @Override
    public TowerType getType() {
        return inner.getType();
    }

    @Override
    public int getLevel() {
        return inner.getLevel();
    }

    @Override
    public int getDamage() {
        return inner.getDamage();
    }

    @Override
    public float getRange() {
        return inner.getRange();
    }

    @Override
    public float getCooldown() {
        return inner.getCooldown();
    }

    @Override
    public int getCost() {
        return inner.getCost();
    }

    @Override
    public TargetingStrategy getTargetingStrategy() {
        return inner.getTargetingStrategy();
    }

    @Override
    public AttackStrategy getAttackStrategy() {
        return inner.getAttackStrategy();
    }

    @Override
    public Set<EnemyComponent> getEnemiesInRange() {
        return inner.getEnemiesInRange();
    }

    @Override
    public void onEnemyEnteredRange(EnemyComponent enemy) {
        inner.onEnemyEnteredRange(enemy);
    }

    @Override
    public void onEnemyExitedRange(EnemyComponent enemy) {
        inner.onEnemyExitedRange(enemy);
    }

    @Override
    public boolean canFire() {
        return inner.canFire();
    }

    @Override
    public void resetCooldown() {
        inner.resetCooldown();
    }

    @Override
    public void tickCooldown(float delta) {
        inner.tickCooldown(delta);
    }

    @Override
    public void update(float delta) {
        inner.update(delta);
    }
}

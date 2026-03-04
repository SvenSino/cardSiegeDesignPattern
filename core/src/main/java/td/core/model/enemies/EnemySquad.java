package td.core.model.enemies;

import com.badlogic.gdx.math.Vector2;
import td.core.model.towers.TowerComponent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EnemySquad implements EnemyComponent {
    private final List<Enemy> members;
    private final int initialSize;

    public EnemySquad(List<Enemy> members) {
        this.members = new ArrayList<>(members);
        this.initialSize = members.size();
    }

    @Override
    public void update(float delta) {
        for (Enemy member : members) {
            member.update(delta);
        }
    }

    @Override
    public void takeDamage(int amount) {
        for (Enemy member : members) {
            member.takeDamage(amount);
        }
    }

    @Override
    public boolean isDead() {
        return members.stream().allMatch(Enemy::isDead);
    }

    @Override
    public boolean hasReachedGoal() {
        return members.stream().anyMatch(Enemy::hasReachedGoal);
    }

    @Override
    public void applySlow(float duration, float factor) {
        for (Enemy member : members) {
            member.applySlow(duration, factor);
        }
    }

    @Override
    public Vector2 getPosition() {
        return frontmost().getPosition();
    }

    @Override
    public int getHp() {
        return members.stream().mapToInt(Enemy::getHp).sum();
    }

    @Override
    public int getMaxHp() {
        return members.stream().mapToInt(Enemy::getMaxHp).sum();
    }

    @Override
    public int getPathIndex() {
        return members.stream().mapToInt(Enemy::getPathIndex).max().orElse(0);
    }

    @Override
    public EnemyType getType() {
        return members.get(0).getType();
    }

    @Override
    public boolean isInRange(TowerComponent tower) {
        return frontmost().isInRange(tower);
    }

    @Override
    public List<EnemyComponent> getLeaves() {
        return new ArrayList<>(members);
    }

    @Override
    public int getGoldValue() {
        return initialSize;
    }

    private Enemy frontmost() {
        return members.stream()
            .max(Comparator.comparingInt(Enemy::getPathIndex))
            .orElse(members.get(0));
    }
}

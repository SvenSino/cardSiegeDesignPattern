package td.core.model.enemies;

import td.core.model.game.GameManager;
import td.core.model.towers.TowerComponent;
import com.badlogic.gdx.math.Vector2;
import lombok.Getter;

import java.util.List;

@Getter
public class Enemy {
    private final EnemyType type;
    private final List<Vector2> path;
    private int pathIndex;
    private final Vector2 position;
    private final float speed;
    private int hp;
    private final int maxHp;
    private boolean reachedGoal;
    private float slowTimer;
    private float slowFactor = 1f;

    public Enemy(EnemyType type, List<Vector2> path, float speed, int hp) {
        this.type = type;
        this.path = path;
        this.speed = speed;
        this.hp = hp;
        this.maxHp = hp;
        this.pathIndex = 0;
        this.position = new Vector2(path.getFirst());
    }

    public void update(float delta) {
        if (pathIndex >= path.size() - 1) {
            reachedGoal = true;
            return;
        }
        Vector2 target = path.get(pathIndex + 1);
        Vector2 direction = new Vector2(target).sub(position);
        float distance = direction.len();
        if (distance < 0.01f) {
            pathIndex++;
            return;
        }
        direction.nor();
        if (slowTimer > 0f) {
            slowTimer -= delta;
            if (slowTimer <= 0f) {
                slowFactor = 1f;
            }
        }
        float step = speed * slowFactor * delta;
        if (step >= distance) {
            position.set(target);
            pathIndex++;
        } else {
            position.mulAdd(direction, step);
        }
    }

    public void takeDamage(int amount) {
        hp -= amount;
    }

    public boolean isDead() {
        return hp <= 0;
    }

    public void applySlow(float duration, float factor) {
        slowTimer = Math.max(slowTimer, duration);
        slowFactor = Math.min(slowFactor, factor);
    }

    public boolean hasReachedGoal() {
        return reachedGoal;
    }

    public boolean isInRange(TowerComponent tower) {
        float dx = position.x - (tower.getGridX() + 0.5f) * GameManager.get().getBoard().getTileSize();
        float dy = position.y - (tower.getGridY() + 0.5f) * GameManager.get().getBoard().getTileSize();
        return (dx * dx + dy * dy) <= tower.getRange() * tower.getRange();
    }

}

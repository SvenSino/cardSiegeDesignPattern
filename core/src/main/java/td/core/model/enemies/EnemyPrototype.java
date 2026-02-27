package td.core.model.enemies;

import java.util.List;
import lombok.Getter;

@Getter
public class EnemyPrototype {
    private final EnemyType type;
    private final float speed;
    private final int hp;

    public EnemyPrototype(EnemyType type, float speed, int hp) {
        this.type = type;
        this.speed = speed;
        this.hp = hp;
    }

    public Enemy copy(List<com.badlogic.gdx.math.Vector2> path) {
        return new Enemy(type, path, speed, hp);
    }
}

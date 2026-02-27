package td.core.model.enemies;

import com.badlogic.gdx.graphics.Color;
import lombok.Getter;

@Getter
public enum EnemyType {
    STANDARD(new Color(0.9f, 0.35f, 0.35f, 1f)),
    FAST(new Color(0.95f, 0.78f, 0.25f, 1f)),
    TANK(new Color(0.58f, 0.42f, 0.9f, 1f));

    private final Color color;

    EnemyType(Color color) {
        this.color = color;
    }
}

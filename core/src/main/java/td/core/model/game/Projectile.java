package td.core.model.game;

import com.badlogic.gdx.math.Vector2;
import lombok.Getter;

@Getter
public class Projectile {
    private final Vector2 from;
    private final Vector2 to;
    private float ttl;

    public Projectile(Vector2 from, Vector2 to, float ttl) {
        this.from = new Vector2(from);
        this.to = new Vector2(to);
        this.ttl = ttl;
    }

    public void update(float delta) {
        ttl -= delta;
    }

    public boolean isExpired() {
        return ttl <= 0f;
    }

    public float getAlpha() {
        if (ttl <= 0f) {
            return 0f;
        }
        return Math.min(1f, ttl / 0.15f);
    }
}
package td.core.model.towers;

import com.badlogic.gdx.graphics.Color;
import lombok.Getter;

public enum TowerType {
    SCOUT(new Color(0.35f, 0.7f, 0.9f, 1f)),
    SNIPER(new Color(0.75f, 0.55f, 0.95f, 1f)),
    RAPID(new Color(0.35f, 0.9f, 0.55f, 1f));

    @Getter
    private final Color color;

    TowerType(Color color) {
        this.color = color;
    }
}
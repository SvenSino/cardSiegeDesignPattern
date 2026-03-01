package td.core.model.towers;

import td.core.model.enemies.Enemy;
import java.util.List;

/**
 * Strategy-Interface für die Zielwahl eines Turms.
 * <p>
 * Kapselt die Entscheidung, <em>welchen</em> Gegner ein Turm angreift.
 * Konkrete Implementierungen: {@code ClosestToGoalStrategy} (vorderster Gegner),
 * {@code HighestHpStrategy} (stärkster Gegner).
 * </p>
 */
public interface TargetingStrategy {

    /**
     * Wählt ein Ziel aus der Liste der verfügbaren Gegner aus.
     *
     * @param tower   der angreifende Turm (für Reichweitenfilterung)
     * @param enemies alle aktuell im Spiel befindlichen Gegner
     * @return das gewählte Ziel, oder {@code null} wenn keines in Reichweite ist
     */
    Enemy selectTarget(TowerComponent tower, List<Enemy> enemies);
}
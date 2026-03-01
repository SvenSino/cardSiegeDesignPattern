package td.core.model.towers;

import td.core.model.enemies.Enemy;

/**
 * Strategy-Interface für das Angriffsverhalten eines Turms.
 * <p>
 * Kapselt die Entscheidung, <em>wie</em> ein Turm angreift.
 * Konkrete Implementierungen: {@code SingleTargetAttackStrategy} (Einzelziel),
 * {@code SplashAttackStrategy} (Flächenschaden um das Primärziel).
 * </p>
 * <p>
 * Beide Implementierungen delegieren die Zielwahl intern an die
 * {@link TargetingStrategy} des Turms. Der Aufruf von
 * {@code tower.getDamage()} traversiert dabei transparent die
 * gesamte Decorator-Kette des Turms.
 * </p>
 */
public interface AttackStrategy {

    /**
     * Führt einen Angriff des Turms aus und gibt das Primärziel zurück.
     *
     * @param tower der angreifende Turm
     * @return getroffener Gegner, oder {@code null} wenn kein Ziel verfügbar war
     */
    Enemy attack(TowerComponent tower);
}

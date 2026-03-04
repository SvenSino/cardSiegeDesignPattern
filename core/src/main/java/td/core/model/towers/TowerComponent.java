package td.core.model.towers;

import td.core.model.enemies.EnemyComponent;

import java.util.Set;

/**
 * Gemeinsames Interface für Türme und Dekoratoren (Decorator-Pattern).
 * <p>
 * Sowohl {@code BaseTower} als auch alle {@code TowerDecorator}-Subklassen
 * implementieren dieses Interface. Aufrufer wie {@code GameManager} und
 * {@code AttackStrategy} arbeiten ausschließlich gegen dieses Interface
 * und sind damit transparent gegenüber beliebig tiefen Decorator-Ketten.
 * </p>
 */
public interface TowerComponent {
    int getGridX();
    int getGridY();
    String getName();
    TowerType getType();

    /** @return aktuelles Upgrade-Level des Turms (0 = kein Upgrade) */
    int getLevel();

    /** @return kumulierter Schadenswert inkl. aller aktiven Decorator-Boni */
    int getDamage();

    /** @return Reichweite in Weltpixeln inkl. aller aktiven Decorator-Boni */
    float getRange();

    float getCooldown();
    int getCost();
    TargetingStrategy getTargetingStrategy();
    AttackStrategy getAttackStrategy();

    /** @return Menge der Gegner, die sich aktuell in Reichweite befinden */
    Set<EnemyComponent> getEnemiesInRange();

    /** Wird vom {@code GameManager} aufgerufen, wenn ein Gegner die Reichweite betritt. */
    void onEnemyEnteredRange(EnemyComponent enemy);

    /** Wird vom {@code GameManager} aufgerufen, wenn ein Gegner die Reichweite verlässt. */
    void onEnemyExitedRange(EnemyComponent enemy);

    /** @return {@code true}, wenn der Cooldown abgelaufen ist und der Turm feuern darf */
    boolean canFire();

    void resetCooldown();

    /**
     * Verringert den verbleibenden Cooldown.
     *
     * @param delta vergangene Zeit seit dem letzten Frame in Sekunden
     */
    void tickCooldown(float delta);

    void update(float delta);
}

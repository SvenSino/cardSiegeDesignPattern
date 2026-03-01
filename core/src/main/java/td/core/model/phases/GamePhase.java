package td.core.model.phases;

import td.core.model.game.GameManager;
/**
 * State-Pattern-Interface für eine Spielphase.
 * <p>
 * Jede Phase kapselt ihr Verhalten vollständig in einer eigenen Klasse.
 * Der {@code GameManager} delegiert {@code update()} an die aktive Phase
 * und wechselt sie über {@code switchPhase()} aus, ohne Phasenlogik
 * selbst zu kennen.
 * </p>
 */
public interface GamePhase {

    /**
     * @return Name der Phase (wird für Events und UI-Anzeige verwendet)
     */
    String getName();

    /**
     * Wird einmalig beim Betreten der Phase aufgerufen.
     * Enthält Initialisierungslogik, z.B. Karten ziehen oder Timer zurücksetzen.
     *
     * @param manager der zentrale Spielzustand
     */
    void enter(GameManager manager);

    /**
     * Wird jeden Frame aufgerufen, solange diese Phase aktiv ist.
     *
     * @param manager der zentrale Spielzustand
     * @param delta   vergangene Zeit seit dem letzten Frame in Sekunden
     */
    void update(GameManager manager, float delta);
}
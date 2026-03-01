package td.core.model.events;

/**
 * Observer-Interface für Spielereignisse.
 * <p>
 * Jede Klasse, die auf Ereignisse des {@link GameEventBus} reagieren soll,
 * implementiert dieses Interface und registriert sich über
 * {@code GameEventBus.subscribe()}. Aktuell einzige Implementierung:
 * {@code GameEventLogger}.
 * </p>
 */
public interface GameEventListener {

    /**
     * Wird synchron aufgerufen, wenn ein Ereignis im {@link GameEventBus}
     * publiziert wird.
     *
     * @param event das eingetretene Ereignis
     */
    void onEvent(GameEvent event);
}
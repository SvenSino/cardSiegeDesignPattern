package td.core.model.events;

import java.util.ArrayList;
import java.util.List;

/**
 * Zentraler Event-Bus des Observer-Patterns.
 * <p>
 * Verwaltet eine Liste von {@link GameEventListener}-Implementierungen und
 * benachrichtigt sie synchron bei jedem publizierten Ereignis. Sender und
 * Empfänger sind vollständig entkoppelt: Der {@code GameManager} kennt
 * keine konkreten Listener, Listener registrieren sich selbst über
 * {@link #subscribe}.
 * </p>
 */
public class GameEventBus {
    private final List<GameEventListener> listeners = new ArrayList<>();

    /**
     * Registriert einen neuen Subscriber.
     *
     * @param listener der zu registrierende Listener
     */
    public void subscribe(GameEventListener listener) {
        listeners.add(listener);
    }

    /**
     * Benachrichtigt alle registrierten Listener synchron.
     *
     * @param event das eingetretene Ereignis
     */
    public void publish(GameEvent event) {
        for (GameEventListener listener : listeners) {
            listener.onEvent(event);
        }
    }
}
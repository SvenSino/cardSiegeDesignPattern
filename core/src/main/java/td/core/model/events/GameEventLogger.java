package td.core.model.events;

public class GameEventLogger implements GameEventListener {
    @Override
    public void onEvent(GameEvent event) {
        System.out.println("[Event] " + event.getName());
    }
}
# Observer – GameEventBus

**Kategorie:** Verhaltensmuster
**Beteiligte Klassen:** `GameEventBus`, `GameEventListener`, `GameEvent`, `GameEventLogger`, `GameManager`

---

## Allgemeine Erklärung

Das Observer-Pattern definiert eine **Eins-zu-viele-Abhängigkeit** zwischen Objekten: Wenn ein Objekt seinen Zustand ändert, werden alle abhängigen Objekte automatisch benachrichtigt. Das Besondere dabei ist die **Richtung der Abhängigkeit**: Nicht das beobachtete Objekt kennt seine Beobachter – die Beobachter registrieren sich selbst.

**Kerngedanke:** Das Subjekt (hier: `GameEventBus`) verwaltet eine Liste von Observern (`GameEventListener`). Es kennt die Observer nur über das Interface, nicht als konkrete Klassen. Wenn ein Ereignis eintreten soll, ruft das Subjekt `publish()` auf, und alle registrierten Observer werden benachrichtigt.

**Vorteile:**
- Sender und Empfänger sind vollständig entkoppelt
- Neue Observer können ohne Änderung am Sender registriert werden
- Das Open/Closed Principle gilt vollständig: neues reaktives System = neues `subscribe()`

---

## Beteiligte Rollen (GoF)

| Rolle | Beschreibung | Im Projekt |
|---|---|---|
| Subject | Verwaltet Observer-Liste, publiziert Events | `GameEventBus` |
| Observer | Interface mit der Benachrichtigungsmethode | `GameEventListener` |
| ConcreteObserver | Reagiert auf Events | `GameEventLogger` |
| Event / Notification | Das Ereignisobjekt | `GameEvent` |
| Client | Registriert Observer, publiziert Events | `GameManager` |

---

## Implementierung in CardSiege

### Das Event-Objekt

```java
public class GameEvent {
    // Statische Konstanten für Events ohne Parameter
    public static final GameEvent WaveStarted = new GameEvent("WaveStarted");
    public static final GameEvent EnemyDefeated = new GameEvent("EnemyDefeated");

    private final String name;

    private GameEvent(String name) {
        this.name = name;
    }

    // Factory-Methoden für Events mit Payload
    public static GameEvent phaseChanged(String phaseName) {
        return new GameEvent("PhaseChanged:" + phaseName);
    }

    public static GameEvent cardPlayed(String cardName) {
        return new GameEvent("CardPlayed:" + cardName);
    }

    public static GameEvent towerBuilt(int x, int y) {
        return new GameEvent("TowerBuilt:" + x + "," + y);
    }

    public static GameEvent baseDamaged(int remainingHealth) {
        return new GameEvent("BaseDamaged:" + remainingHealth);
    }
}
```

Statt einfacher Strings verwendet das System typisierte Factory-Methoden. Das verhindert Magic-Strings wie `"tower_built"` oder `"PHASE_CHANGED"` an verschiedenen Stellen im Code. `GameEvent` ist ein einfaches, unveränderliches Wertobjekt.

### Der Observer-Bus

```java
public class GameEventBus {
    private final List<GameEventListener> listeners = new ArrayList<>();

    public void subscribe(GameEventListener listener) {
        listeners.add(listener);
    }

    public void publish(GameEvent event) {
        for (GameEventListener listener : listeners) {
            listener.onEvent(event);  // synchrone Benachrichtigung
        }
    }
}
```

Minimale Implementierung: eine Liste, `subscribe()` und `publish()`. Die Benachrichtigung erfolgt synchron in derselben Frame-Iteration.

### Das Observer-Interface

```java
public interface GameEventListener {
    void onEvent(GameEvent event);
}
```

Ein einziges Interface mit einer Methode. Jeder, der auf Events reagieren will, implementiert dieses Interface.

### Der einzige aktive Subscriber: GameEventLogger

```java
public class GameEventLogger implements GameEventListener {
    @Override
    public void onEvent(GameEvent event) {
        System.out.println("[Event] " + event.getName());
    }
}
```

Gibt jeden Event auf der Konsole aus. Einfach, aber ausreichend, um den gesamten Spielverlauf nachvollziehbar zu machen.

---

## Registrierung im Singleton-Konstruktor

```java
public class GameManager {
    private final GameEventBus eventBus = new GameEventBus();

    private GameManager() {
        // Observer sofort beim Erzeugen des Singletons registrieren
        eventBus.subscribe(new GameEventLogger());
    }
}
```

Der `GameEventLogger` wird direkt im privaten Konstruktor registriert – noch bevor `init()` aufgerufen wird. Das Logging-System ist von der ersten Spielaktion an aktiv, ohne explizite Initialisierungsreihenfolge nach außen.

---

## Alle publizierten Events im Überblick

Events werden an sechs verschiedenen Stellen im `GameManager` publiziert:

```java
// 1. Phasenwechsel – jedes Mal wenn switchPhase() aufgerufen wird
public void switchPhase(GamePhase newPhase) {
    ...
    eventBus.publish(GameEvent.phaseChanged(newPhase.getName()));
    // Beispiel-Output: "[Event] PhaseChanged:Wave"
}

// 2. Karte gespielt
public void playCard(int index) {
    ...
    eventBus.publish(GameEvent.cardPlayed(card.getName()));
    // Beispiel-Output: "[Event] CardPlayed:Overcharge"
}

// 3. Turm gebaut
public boolean tryPlacePendingTower(int gridX, int gridY) {
    ...
    eventBus.publish(GameEvent.towerBuilt(gridX, gridY));
    // Beispiel-Output: "[Event] TowerBuilt:5,8"
}

// 4. Welle gestartet
public void beginWave() {
    ...
    eventBus.publish(GameEvent.WaveStarted);
    // Beispiel-Output: "[Event] WaveStarted"
}

// 5. Gegner besiegt
private void cleanupEntities() {
    enemies.removeIf(enemy -> {
        if (enemy.isDead()) {
            ...
            eventBus.publish(GameEvent.EnemyDefeated);
            // Beispiel-Output: "[Event] EnemyDefeated"
        }
        ...
    });
}

// 6. Basis beschädigt
private void cleanupEntities() {
    ...
    if (enemy.hasReachedGoal()) {
        baseHealth -= 1;
        eventBus.publish(GameEvent.baseDamaged(baseHealth));
        // Beispiel-Output: "[Event] BaseDamaged:7"
    }
}
```

---

## Erweiterbarkeit

Die Architektur ist für beliebig viele weitere Subscriber offen. Ein Sound-System, ein Achievement-Tracker oder eine Statistik-Komponente könnten sich mit einem einzigen `subscribe()`-Aufruf registrieren:

```java
// Hypothetisch – kein Code-Change am GameManager nötig
eventBus.subscribe(new SoundManager());
eventBus.subscribe(new AchievementSystem());
eventBus.subscribe(new StatsTracker());
```

Der `GameManager` würde davon nichts wissen und nichts ändern müssen. Das ist der fundamentale Unterschied zu direkten Methodenaufrufen: Die Erweiterbarkeit ist strukturell garantiert, nicht nur theoretisch möglich.

---

## Zusammenspiel mit anderen Patterns

- **Singleton:** Der `GameEventBus` ist ein Feld des Singletons. Registrierung und Publikation finden immer über dieselbe Instanz statt.
- **State:** Jeder `switchPhase()`-Aufruf – also jeder Zustandswechsel des State-Patterns – publiziert automatisch ein `PhaseChanged`-Event. Die beiden Patterns sind eng verzahnt: State steuert den Spielfluss, Observer dokumentiert ihn.
- **Command:** Nach jedem `card.execute()` publiziert der Invoker (`playCard()`) ein `CardPlayed`-Event. Das Logging-System erfasst jede Spielaktion, ohne dass die Karten-Klassen selbst Events kennen müssen.
- **Builder:** Wenn `tryPlacePendingTower()` erfolgreich ist (Builder-Schritt 2), wird ein `TowerBuilt`-Event publiziert. Der Observer-Bus ist der "Zeuge" des Builder-Abschlusses.

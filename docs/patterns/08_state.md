# State – Spielphasenzyklus

**Kategorie:** Verhaltensmuster
**Beteiligte Klassen:** `GamePhase` (Interface), `DrawPhase`, `PlayPhase`, `WavePhase`, `ResolvePhase`, `GameOverPhase`, `GameManager`

---

## Allgemeine Erklärung

Das State-Pattern ermöglicht es einem Objekt, sein **Verhalten zu ändern, wenn sich sein interner Zustand ändert** – so, als würde das Objekt seine Klasse wechseln. Statt einer großen `switch`- oder `if-else`-Kette in der Hauptklasse wird das zustandsabhängige Verhalten in eigene Klassen ausgelagert.

**Kerngedanke:** Jeder Zustand ist eine eigene Klasse, die ein gemeinsames Interface implementiert. Das Kontextobjekt (hier: `GameManager`) hält eine Referenz auf den aktuellen Zustand und delegiert Methodenaufrufe daran. Bei einem Zustandswechsel wird einfach die Referenz ausgetauscht.

**Vorteile:**
- Zustandsabhängiges Verhalten ist lokalisiert und klar abgegrenzt
- Neue Zustände können ohne Änderung bestehender Klassen hinzugefügt werden
- Keine wachsenden `if-else`-Kaskaden

---

## Beteiligte Rollen (GoF)

| Rolle | Beschreibung | Im Projekt |
|---|---|---|
| Context | Hält den aktuellen Zustand, delegiert an ihn | `GameManager` |
| State | Interface mit den Zustandsmethoden | `GamePhase` |
| ConcreteState | Implementiert das Verhalten für einen Zustand | `DrawPhase`, `PlayPhase`, `WavePhase`, `ResolvePhase`, `GameOverPhase` |

---

## Das State-Interface

```java
public interface GamePhase {
    String getName();
    void enter(GameManager manager);          // einmalig beim Betreten
    void update(GameManager manager, float delta);  // jeden Frame
}
```

Drei Methoden:
- `getName()` – gibt den Phasennamen zurück (für Events und UI)
- `enter()` – wird einmalig beim Phasenwechsel aufgerufen, enthält Initialisierungslogik
- `update()` – wird jeden Frame aufgerufen, enthält laufendes Verhalten

---

## Die fünf Phasenklassen

### DrawPhase – Kartenziehen

```java
public class DrawPhase implements GamePhase {
    @Override
    public String getName() { return "Draw"; }

    @Override
    public void enter(GameManager manager) {
        manager.drawHand(3);        // 3 Karten ziehen
        manager.resetEnergy(3);     // Energie auf 3 zurücksetzen
        manager.switchPhase(new PlayPhase());  // sofort weiterleiten
    }

    @Override
    public void update(GameManager manager, float delta) {
        // leer – die Phase dauert keinen Frame, sie ist in enter() abgeschlossen
    }
}
```

Die `DrawPhase` hat eine leere `update()`-Methode, weil sie ihre gesamte Logik in `enter()` erledigt und sofort zur `PlayPhase` weiterleitet. Sie ist konzeptionell eine Übergangsphase.

### PlayPhase – Kartenausspielen

```java
public class PlayPhase implements GamePhase {
    @Override
    public String getName() { return "Play"; }

    @Override
    public void enter(GameManager manager) {
        // leer – keine Initialisierung nötig
    }

    @Override
    public void update(GameManager manager, float delta) {
        // leer – wartet ausschließlich auf Nutzerinteraktion
    }
}
```

Beide Methoden leer: Die `PlayPhase` ist vollständig passiv. Sie reagiert nur auf externe Events – der Spieler spielt Karten oder drückt auf "Wave starten". Der Übergang zur `WavePhase` wird vom `GameScreen` ausgelöst:

```java
// GameScreen.java – Spieler drückt "Wave"-Button
manager.requestWaveStart();

// GameManager
public void requestWaveStart() {
    if (phase instanceof PlayPhase) {
        switchPhase(new WavePhase());
    }
}
```

### WavePhase – Gegnerwelle

```java
public class WavePhase implements GamePhase {
    @Override
    public String getName() { return "Wave"; }

    @Override
    public void enter(GameManager manager) {
        manager.beginWave();  // WaveSpawner starten
    }

    @Override
    public void update(GameManager manager, float delta) {
        manager.endWaveIfComplete();  // prüfen ob Welle abgeschlossen
    }
}
```

In `enter()` wird der `WaveSpawner` gestartet. In `update()` wird jeden Frame geprüft, ob alle Gegner gespawnt und besiegt wurden.

### ResolvePhase – Auflösung nach der Welle

```java
public class ResolvePhase implements GamePhase {
    private float timer;

    @Override
    public String getName() { return "Resolve"; }

    @Override
    public void enter(GameManager manager) {
        timer = 0f;  // Timer zurücksetzen
    }

    @Override
    public void update(GameManager manager, float delta) {
        timer += delta;
        if (timer > 0.8f) {
            manager.switchPhase(new DrawPhase());  // nach 0.8s neuer Zug
        }
    }
}
```

Die `ResolvePhase` ist die einzige Phase mit einem internen Timer. Sie hält das Spiel für 0,8 Sekunden an – genug Zeit für visuelle Rückmeldung – und leitet dann automatisch den nächsten Zug ein.

### GameOverPhase – Spielende

```java
public class GameOverPhase implements GamePhase {
    private final boolean victory;

    public GameOverPhase(boolean victory) {
        this.victory = victory;
    }

    @Override
    public String getName() { return victory ? "Victory" : "Defeat"; }

    @Override
    public void enter(GameManager manager) { }

    @Override
    public void update(GameManager manager, float delta) { }

    public boolean isVictory() { return victory; }
}
```

Beide Methoden leer: Das Spiel ist eingefroren, kein automatischer Übergang findet mehr statt. Der `GameManager` prüft zu Beginn von `update()`, ob die Phase `GameOverPhase` ist, und kehrt sofort zurück.

---

## Phasenwechsel im GameManager

```java
public void switchPhase(GamePhase newPhase) {
    this.phase = newPhase;
    phase.enter(this);  // enter() der neuen Phase sofort aufrufen
    eventBus.publish(GameEvent.phaseChanged(newPhase.getName()));  // Observer benachrichtigen
}
```

Drei Schritte: Referenz ersetzen, `enter()` aufrufen, Event publizieren. Der `GameManager` weiß nicht, welche Phase er betritt – er ruft nur `enter()` auf dem neuen Objekt auf.

### Der Phasenzyklus

```
[Spielstart]
     |
     v
  DrawPhase.enter()
  → drawHand(3), resetEnergy(3)
  → switchPhase(PlayPhase)
     |
     v
  PlayPhase          <-- Spieler spielt Karten
  → requestWaveStart() → switchPhase(WavePhase)
     |
     v
  WavePhase.enter()
  → beginWave()      <-- Gegner spawnen
  WavePhase.update()
  → endWaveIfComplete()
     |
     +-- Welle läuft noch: continue
     |
     +-- Alle 12 Wellen: switchPhase(GameOverPhase(true))  → SIEG
     |
     +-- Welle komplett: switchPhase(ResolvePhase)
           |
           v
        ResolvePhase   <-- 0.8s warten
        → switchPhase(DrawPhase)  → nächster Zug
     |
     +-- baseHealth <= 0: switchPhase(GameOverPhase(false)) → NIEDERLAGE
```

---

## Phasenabhängige Spielregeln

Das State-Pattern macht phasenabhängige Regeln klar lokalisierbar. Im `GameManager.playCard()`:

```java
public void playCard(int index) {
    // Nur in Play oder Wave spielbar
    if (!(phase instanceof PlayPhase) && !(phase instanceof WavePhase)) return;
    ...
    if (!canPlayCard(card)) { ... }
}

public boolean canPlayCard(Card card) {
    boolean wavePhase = phase instanceof WavePhase;
    boolean buildCard = card instanceof BuildTowerCard;

    // BuildTowerCard in der WavePhase gesperrt
    if (wavePhase && buildCard) return false;
    ...
}
```

Ohne State-Pattern wäre diese Logik ein wachsendes Geflecht aus Bedingungen, das mit jeder neuen Phase schwerer zu verstehen und zu ändern wäre.

---

## Zusammenspiel mit anderen Patterns

- **Observer:** Jeder `switchPhase()`-Aufruf publiziert automatisch ein `PhaseChanged`-Event über den `GameEventBus`. Das Logging des `GameEventLogger` erfasst dadurch jeden Phasenwechsel, ohne dass die Phasenklassen den Logger kennen.
- **Command:** Das State-Pattern kontrolliert, welche Commands (Karten) ausführbar sind. `playCard()` prüft `phase instanceof PlayPhase || WavePhase`, bevor es `card.execute()` aufruft.
- **Singleton:** Die aktuelle Phase ist ein Feld des Singletons (`GameManager.phase`). `switchPhase()` ist die einzige Methode, die dieses Feld ändert.
- **Builder:** `WavePhase.enter()` ruft `beginWave()` auf, das den `WaveSpawner` startet. Der `WaveSpawner` konfiguriert die `EnemyPrototype`-Objekte – Prototype-Pattern ausgelöst durch einen State-Übergang.

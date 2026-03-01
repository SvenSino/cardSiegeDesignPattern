# Singleton – GameManager

**Kategorie:** Erzeugungsmuster
**Beteiligte Klassen:** `GameManager`

---

## Allgemeine Erklärung

Das Singleton-Pattern stellt sicher, dass von einer Klasse **genau eine Instanz** existiert und diese global zugänglich ist. Es besteht aus drei Teilen:

1. **Privater Konstruktor** – verhindert, dass externer Code `new Klasse()` aufruft
2. **Statisches, privates Feld** – hält die einzige Instanz
3. **Statische Zugriffsmethode** – gibt die Instanz zurück (`getInstance()`, `get()` o.ä.)

Das Muster löst das Problem des unkontrollierten Erzeugens von Objekten, bei denen es konzeptionell nur eines geben darf – z.B. eine Konfiguration, ein Logging-System oder eben ein zentraler Spielzustand.

**Varianten:**
- *Eager Initialization*: Instanz wird beim Klassenladen sofort erzeugt (thread-safe by default in Java)
- *Lazy Initialization*: Instanz wird erst beim ersten `get()`-Aufruf erzeugt (erfordert Synchronisierung bei Multithreading)

---

## Beteiligte Rollen (GoF)

| Rolle | Beschreibung | Im Projekt |
|---|---|---|
| Singleton | Klasse mit privatem Konstruktor, statischer Instanz und Zugriffsmethode | `GameManager` |

---

## Implementierung in CardSiege

CardSiege besitzt zu jedem Zeitpunkt genau einen Spielzustand. Türme, Gegner, Ressourcen, aktive Phase und Ereignisbus müssen alle konsistent von einer einzigen Stelle verwaltet werden. Würden mehrere `GameManager`-Instanzen existieren, könnten Türme in einer Instanz registriert, aber Gegner in einer anderen verwaltet werden – das Spiel wäre nicht mehr deterministisch steuerbar.

```java
public class GameManager {

    // Einzige Instanz, sofort beim Klassenladen erzeugt
    private static final GameManager INSTANCE = new GameManager();

    // Einziger Zugang von außen
    public static GameManager get() {
        return INSTANCE;
    }

    // Privater Konstruktor – kein new GameManager() von außen möglich
    private GameManager() {
        eventBus.subscribe(new GameEventLogger()); // Observer sofort aktiv
    }
}
```

Der Konstruktor ist `private` – es ist unmöglich, versehentlich eine zweite Instanz zu erzeugen. Das statische Feld `INSTANCE` wird als `final` deklariert, kann also nach der Initialisierung nicht mehr überschrieben werden.

**Besonderheit:** Der private Konstruktor registriert sofort den `GameEventLogger` am `GameEventBus`. Das bedeutet, das Observer-System ist vom ersten Moment an aktiv – noch bevor `init()` aufgerufen wird.

### Nutzung im Spiel

```java
// GameScreen – einmalige Initialisierung zu Spielbeginn
GameManager.get().init(board, new StrategyCardFactory());

// Karten greifen über CardContext auf den Manager zu
public void execute(CardContext context) {
    context.getManager().applyDamageBuff(bonusDamage);
}

// Phasenklassen kennen den Manager durch Methodenparameter,
// nicht durch direkten Singleton-Zugriff
public void enter(GameManager manager) {
    manager.drawHand(3);
    manager.switchPhase(new PlayPhase());
}
```

### Zustand, den der GameManager verwaltet

```java
private Board board;
private int energy;           // aktuelle Energie des Spielers
private int gold;             // aktuelles Gold
private int baseHealth;       // Lebenspunkte der Basis (Start: 10)
private int wavesCompleted;   // abgeschlossene Wellen
private int maxWaves;         // Siegbedingung (12)
private final List<TowerComponent> towers;
private final List<Enemy> enemies;
private final List<Projectile> projectiles;
private Deck deck;
private Hand hand;
private GamePhase phase;      // aktueller Spielzustand (State-Pattern)
private PendingTower pendingTower;
private final GameEventBus eventBus;
private final WaveSpawner waveSpawner;
```

Alle diese Felder existieren genau einmal – das ist die Kernaussage des Singletons im Kontext dieses Spiels.

---

## Zusammenspiel mit anderen Patterns

- **Observer:** Der `GameEventBus` ist ein Feld des Singletons. Der `GameEventLogger` wird direkt im privaten Konstruktor registriert und ist dadurch systemweit und sofort aktiv.
- **State:** Das `phase`-Feld des Singletons hält den aktuellen Spielzustand. `switchPhase()` ist die einzige Methode, die diesen Zustand wechselt.
- **Command:** Karten greifen über `CardContext.getManager()` auf den Singleton zu, ohne ihn direkt zu referenzieren. `CardContext` ist ein dünner Wrapper, der die Abhängigkeit explizit macht.
- **Factory:** `init()` nimmt eine `CardFactory` entgegen – der Singleton kennt keine konkrete Fabrik, nur das Interface.

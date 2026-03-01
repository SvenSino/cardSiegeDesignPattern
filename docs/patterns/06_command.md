# Command – Kartensystem

**Kategorie:** Verhaltensmuster
**Beteiligte Klassen:** `Card` (abstrakt), alle 8 Kartenklassen, `CardContext`, `GameManager`, `GameScreen`

---

## Allgemeine Erklärung

Das Command-Pattern kapselt eine **Aktion als eigenständiges Objekt**. Statt eine Methode direkt aufzurufen, wird die Anforderung als Objekt verpackt – mit allen nötigen Parametern. Der Aufrufer (Invoker) kennt nur das Command-Interface und weiß nicht, was genau passiert. Der Ausführende (Receiver) weiß, wie die Aktion intern realisiert wird, aber nicht, wer sie angefordert hat.

**Vorteile:**
- Invoker und Receiver sind vollständig entkoppelt
- Commands sind parametrierbar und wiederverwendbar
- Grundlage für Undo/Redo, Command-Queues und Logging

**Klassische GoF-Rollen:**
- `Command` – Interface mit `execute()`
- `ConcreteCommand` – implementiert `execute()`, delegiert an Receiver
- `Invoker` – ruft `command.execute()` auf
- `Receiver` – führt die eigentliche Arbeit aus
- `Client` – erzeugt ConcreteCommands und verdrahtet sie mit dem Receiver

---

## Beteiligte Rollen (GoF)

| Rolle | Beschreibung | Im Projekt |
|---|---|---|
| Command | Abstrakte Basisklasse mit `execute()` | `Card` |
| ConcreteCommand | Konkrete Kartenklasse | `DamageBuffCard`, `FreezeCard`, `BuildTowerCard`, ... |
| Invoker | Ruft `card.execute()` auf | `GameManager.playCard()`, ausgelöst durch `GameScreen` |
| Receiver | Führt die Aktion tatsächlich aus | `GameManager` (Methoden wie `applyDamageBuff()`) |
| Client | Erzeugt und konfiguriert Commands | `StrategyCardFactory` |

---

## Implementierung in CardSiege

### Das Command-Interface: abstrakte Klasse `Card`

```java
public abstract class Card {
    private final String name;
    private final int cost;
    private final String description;

    public abstract void execute(CardContext context);  // das Command-Interface
    public abstract Card copy();                        // Prototype-Interface
}
```

`execute(CardContext)` ist die einzige Methode, die jede Karte implementieren muss. `CardContext` ist ein dünner Wrapper, der den Zugang zum Receiver kapselt:

```java
public class CardContext {
    private final GameManager manager;  // der Receiver

    public CardContext(GameManager manager) {
        this.manager = manager;
    }

    public GameManager getManager() {
        return manager;
    }
}
```

### Die acht ConcreteCommands

Jede Karte kennt nur ihren eigenen Parameter und delegiert vollständig an den Receiver:

**DamageBuffCard** – alle Türme erhalten einen Schadensbonus:
```java
public class DamageBuffCard extends Card {
    private final int bonusDamage;

    @Override
    public void execute(CardContext context) {
        context.getManager().applyDamageBuff(bonusDamage);
    }
}
```

**BuildTowerCard** – startet den zweistufigen Turmbau-Prozess (Builder-Pattern):
```java
public class BuildTowerCard extends Card {
    private final int damage;
    private final float range;
    private final float cooldown;
    private final TargetingStrategy targeting;
    private final AttackStrategy attackStrategy;
    private final TowerType type;

    @Override
    public void execute(CardContext context) {
        PendingTower pending = new PendingTower(
            getName(), type, getCost(), damage, range, cooldown,
            targeting, attackStrategy
        );
        context.getManager().queueTowerPlacement(pending);
    }
}
```

Die übrigen Karten folgen demselben Prinzip – ein Parameter, ein Receiver-Aufruf:

| Klasse | Parameter | Receiver-Methode |
|---|---|---|
| `DamageBuffCard` | `bonusDamage: int` | `applyDamageBuff(bonusDamage)` |
| `RangeBuffCard` | `bonusRange: float` | `queueRangeBuff(bonusRange)` |
| `FreezeCard` | `duration: float`, `slowFactor: float` | `slowAllEnemies(duration, slowFactor)` |
| `EnergyCard` | `amount: int` | `addEnergy(amount)` |
| `GoldCard` | `amount: int` | `addGold(amount)` |
| `DrawCard` | `count: int` | `drawHand(count)` |
| `DiscountCard` | `amount: int` | `addNextCardDiscount(amount)` |
| `BuildTowerCard` | `damage`, `range`, `cooldown`, `targeting`, `attackStrategy`, `type` | `queueTowerPlacement(pending)` |

### Der Invoker: GameManager.playCard()

```java
public void playCard(int index) {
    // Nur in Play- oder Wave-Phase spielbar
    if (!(phase instanceof PlayPhase) && !(phase instanceof WavePhase)) return;

    Card card = hand.removeAt(index);
    if (card == null) return;

    if (!canPlayCard(card)) {
        hand.addToFront(card);  // zurücklegen wenn nicht spielbar
        return;
    }

    int effectiveCost = getEffectiveEnergyCost(card);
    energy -= effectiveCost;
    nextCardDiscount = 0;

    card.execute(new CardContext(this));  // Command ausführen
    eventBus.publish(GameEvent.cardPlayed(card.getName()));
}
```

`playCard()` weiß nicht, welche Karte gespielt wird. Es prüft allgemeine Bedingungen (Phase, Energie, Gold) und ruft dann `execute()` auf. Die konkrete Aktion ist vollständig in der Karte gekapselt.

### Der Receiver: GameManager-Methoden

```java
// Receiver kennt die Implementierungsdetails
public void applyDamageBuff(int bonusDamage) {
    applyTowerModifier(tower -> new DamageBuffDecorator(tower, bonusDamage));
}

public void queueRangeBuff(float bonusRange) {
    queueTowerModifier(tower -> new RangeBuffDecorator(tower, bonusRange));
}

public void slowAllEnemies(float duration, float slowFactor) {
    for (Enemy enemy : enemies) {
        enemy.applySlow(duration, slowFactor);
    }
}
```

Die Karten wissen *was* ausgelöst werden soll. Der `GameManager` weiß *wie* es intern umgesetzt wird – z.B. dass `applyDamageBuff` einen `DamageBuffDecorator` erzeugt und alle Turmreferenzen ersetzt.

---

## Der Konfigurationsfluss

```
StrategyCardFactory (Client)
    ↓ erzeugt und konfiguriert ConcreteCommands
DamageBuffCard(bonusDamage=1), FreezeCard(2.0f, 0.4f), ...
    ↓ landen im Deck (via Prototype geklont)
Hand
    ↓ Spieler klickt auf Karte → GameScreen ruft playCard(index) auf
GameManager.playCard() (Invoker)
    ↓ card.execute(new CardContext(this))
DamageBuffCard.execute() → context.getManager().applyDamageBuff(1)
    ↓
GameManager.applyDamageBuff(1) (Receiver)
    ↓ iteriert Turmlist, ersetzt mit DamageBuffDecorator
```

---

## Warum kein Undo?

Das Command-Pattern legt konzeptionell die Grundlage für Undo: Man würde `undo()` auf `Card` hinzufügen. In CardSiege wurde das nicht implementiert, weil Decorator-Ketten nicht trivial rückgängig zu machen sind – `applyDamageBuff()` ersetzt alle Turmreferenzen durch neue Objekte; die alten Referenzen sind weg. Ein vollständiges Undo würde entweder die alten Referenzen persistieren oder das Memento-Pattern erfordern.

---

## Zusammenspiel mit anderen Patterns

- **Decorator:** `applyDamageBuff()` und `queueRangeBuff()` sind die einzigen Stellen, an denen Decorator-Objekte erzeugt werden. Command triggert Decorator – die Karte weiß nicht, *wie* der Buff intern realisiert wird.
- **Factory Method:** Die `StrategyCardFactory` konfiguriert alle ConcreteCommands. Der Client-Teil des Command-Patterns liegt vollständig in der Factory.
- **Prototype:** `Card.copy()` ist das zweite abstrakte Interface neben `execute()`. Karten sind sowohl Commands als auch Prototypen.
- **State:** `playCard()` prüft, ob die aktuelle Phase das Spielen erlaubt (`PlayPhase` oder `WavePhase`). `BuildTowerCard` ist in der `WavePhase` zusätzlich gesperrt. Das State-Pattern bestimmt, welche Commands ausführbar sind.

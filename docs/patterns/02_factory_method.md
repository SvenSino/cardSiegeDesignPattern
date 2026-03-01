# Factory Method – CardFactory

**Kategorie:** Erzeugungsmuster
**Beteiligte Klassen:** `CardFactory`, `StrategyCardFactory`, `Deck`

---

## Allgemeine Erklärung

Das Factory-Method-Pattern definiert eine **Schnittstelle zur Objekterzeugung**, überlässt aber konkreten Unterklassen (oder Implementierungen) die Entscheidung, welche Klasse tatsächlich instanziiert wird. Der Aufrufer arbeitet ausschließlich gegen das Interface – er weiß nicht, welche konkreten Objekte entstehen.

**Kerngedanke:** Erzeugung und Verwendung von Objekten werden in verschiedene Klassen ausgelagert. Die Klasse, die die Objekte braucht (hier: `Deck`), kennt nur das Factory-Interface und bleibt damit offen für neue Implementierungen, ohne geändert werden zu müssen (*Open/Closed Principle*).

**Abgrenzung zu Abstract Factory:** Die Factory Method erzeugt in der Regel eine Produktfamilie über eine einzige Methode. Die Abstract Factory koordiniert mehrere zusammengehörige Fabriken.

---

## Beteiligte Rollen (GoF)

| Rolle | Beschreibung | Im Projekt |
|---|---|---|
| Product | Das erzeugte Objekt (abstrakt) | `Card` (abstrakte Basisklasse) |
| ConcreteProduct | Konkrete Implementierung des Produkts | `BuildTowerCard`, `DamageBuffCard`, `FreezeCard`, ... |
| Creator | Definiert die Factory-Methode als Interface | `CardFactory` |
| ConcreteCreator | Implementiert die Factory-Methode | `StrategyCardFactory` |

---

## Implementierung in CardSiege

Das `Deck` muss Karten erzeugen, ohne zu wissen, welche Karten existieren. Wäre die Erzeugungslogik direkt im `Deck` implementiert, müsste bei jeder neuen Karte der Deck-Code geändert werden – eine direkte Verletzung des Open/Closed Principle.

### Das Factory-Interface

```java
public interface CardFactory {
    List<Card> createDeck();
}
```

Minimalistische Schnittstelle: eine einzige Methode, die eine Liste fertig konfigurierter Karten-Prototypen zurückgibt.

### Die konkrete Implementierung

```java
public class StrategyCardFactory implements CardFactory {
    @Override
    public List<Card> createDeck() {
        List<Card> cards = new ArrayList<>();

        // Turmkarten mit fertigen Strategie-Kombinationen
        cards.add(new BuildTowerCard("Scout Tower",  2, 2, 120f, 0.8f,
            new ClosestToGoalStrategy(), new SingleTargetAttackStrategy(), TowerType.SCOUT));
        cards.add(new BuildTowerCard("Sniper Tower", 3, 4, 180f, 1.4f,
            new HighestHpStrategy(),    new SingleTargetAttackStrategy(), TowerType.SNIPER));
        cards.add(new BuildTowerCard("Rapid Tower",  2, 1, 100f, 0.4f,
            new ClosestToGoalStrategy(), new SplashAttackStrategy(36f, 0.6f), TowerType.RAPID));

        // Ressourcen-, Buff- und Sonderkarten
        cards.add(new EnergyCard("Energy Surge", 0, 2));
        cards.add(new GoldCard("Supply Drop",    0, 3));
        cards.add(new DamageBuffCard("Overcharge", 1, 1));
        cards.add(new RangeBuffCard("Long Range",  1, 40f));
        cards.add(new DrawCard("Tactics",          0, 2));
        cards.add(new DiscountCard("Planning",     0, 1));
        cards.add(new FreezeCard("Cryo Pulse",     1, 2.0f, 0.4f));

        return cards;
    }
}
```

Die gesamte Konfigurationsentscheidung – welche Werte, welche Strategien, welche Kosten – liegt ausschließlich hier. Das `Deck` weiß davon nichts.

### Nutzung im Deck

```java
public class Deck {
    private final CardFactory factory;  // nur das Interface, nie eine konkrete Klasse

    public Deck(CardFactory factory) {
        this.factory = factory;
        refill();
    }

    private void refill() {
        cards.clear();
        for (Card prototype : factory.createDeck()) {
            cards.add(prototype.copy());  // Prototype-Pattern: jede Karte zweimal klonen
            cards.add(prototype.copy());
        }
        Collections.shuffle(cards);
    }
}
```

Das `Deck` iteriert über die Liste, klont jede Karte zweimal (20 Karten insgesamt) und mischt. Es kennt keine einzige konkrete Kartenklasse.

### Injektion des Singletons

```java
// GameScreen übergibt die Factory beim Start
GameManager.get().init(board, new StrategyCardFactory());

// GameManager nimmt nur das Interface entgegen
public void init(Board board, CardFactory cardFactory) {
    this.deck = new Deck(cardFactory);
    ...
}
```

---

## Zusammenspiel mit anderen Patterns

- **Prototype:** Die Factory liefert konfigurierte Prototyp-Objekte. Das `Deck` klont sie über `copy()`. Factory und Prototype teilen sich die Arbeit: Die Factory entscheidet *was* erzeugt wird, Prototype entscheidet *wie* geklont wird.
- **Command:** Alle Karten, die die Factory erzeugt, sind Command-Objekte (`Card extends` ... mit `execute(CardContext)`). Die Factory konfiguriert die Commands vollständig, bevor sie ins Deck wandern.
- **Strategy:** Die Factory verdrahtet die Strategie-Kombinationen in den `BuildTowerCard`-Objekten. `ClosestToGoalStrategy + SingleTargetAttackStrategy` für den Scout, `HighestHpStrategy + SingleTargetAttackStrategy` für den Sniper usw.
- **Singleton:** Der `GameManager` nimmt die Factory über `init()` entgegen. Der Singleton kennt keine konkreten Kartenklassen – die Abhängigkeit wird von außen injiziert.

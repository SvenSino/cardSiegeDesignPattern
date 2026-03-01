# Prototype – Card.copy() und EnemyPrototype

**Kategorie:** Erzeugungsmuster
**Beteiligte Klassen:** `Card` (abstrakt), alle Kartenklassen, `EnemyPrototype`, `Enemy`, `WaveSpawner`

---

## Allgemeine Erklärung

Das Prototype-Pattern ermöglicht die Erzeugung neuer Objekte durch **Klonen eines konfigurierten Vorlageobjekts** (Prototyp), anstatt sie von Grund auf neu zu konfigurieren. Der Aufrufer ruft auf dem Prototyp `copy()` (oder `clone()`) auf und erhält eine unabhängige Kopie mit identischen Attributen.

**Wann ist das sinnvoll?**
- Die Konfiguration eines Objekts ist aufwändig (viele Parameter)
- Viele ähnliche Objekte werden benötigt, die sich nur in geringen Details unterscheiden
- Die konkrete Klasse des Objekts soll dem Aufrufer verborgen bleiben

**Abgrenzung:** Anders als Factory Method oder Builder, bei denen externe Logik die Konfiguration steuert, trägt beim Prototype das Objekt selbst die Verantwortung für seine Kopie. Das Klonen ist eine Eigenschaft des Objekts.

---

## Beteiligte Rollen (GoF)

| Rolle | Beschreibung | Im Projekt (Karten) | Im Projekt (Gegner) |
|---|---|---|---|
| Prototype | Interface / abstrakte Klasse mit `copy()` | `Card` (abstrakt) | `EnemyPrototype` |
| ConcretePrototype | Implementiert `copy()` | `DamageBuffCard`, `BuildTowerCard`, etc. | `EnemyPrototype` selbst |
| Client | Klont den Prototyp | `Deck.refill()` | `WaveSpawner.update()` |

---

## Implementierung in CardSiege – Kartensystem

Die abstrakte Klasse `Card` schreibt `copy()` als abstrakte Methode vor:

```java
public abstract class Card {
    private final String name;
    private final int cost;
    private final String description;

    public abstract void execute(CardContext context);
    public abstract Card copy();  // jede Kartenklasse muss klonen können
}
```

Jede konkrete Kartenklasse implementiert `copy()` indem sie sich selbst mit identischen Parametern neu erzeugt:

```java
public class DamageBuffCard extends Card {
    private final int bonusDamage;

    @Override
    public Card copy() {
        return new DamageBuffCard(getName(), getCost(), bonusDamage);
    }
}

public class BuildTowerCard extends Card {
    private final int damage;
    private final float range;
    private final float cooldown;
    private final TargetingStrategy targeting;
    private final AttackStrategy attackStrategy;
    private final TowerType type;

    @Override
    public Card copy() {
        // Alle Parameter werden 1:1 übernommen – auch die Strategie-Referenzen
        return new BuildTowerCard(getName(), getCost(), damage, range,
                                  cooldown, targeting, attackStrategy, type);
    }
}
```

Das `Deck` ruft `copy()` auf jedem Prototyp **zweimal** auf – so entstehen 20 unabhängige Karten aus 10 Vorlagen:

```java
private void refill() {
    cards.clear();
    for (Card prototype : factory.createDeck()) {
        cards.add(prototype.copy());  // erste Kopie
        cards.add(prototype.copy());  // zweite Kopie – völlig unabhängig
    }
    Collections.shuffle(cards);
}
```

**Warum ist die Unabhängigkeit wichtig?** Würde das `Deck` dieselben Objekte zweimal hinzufügen (statt zu klonen), würde das Ausspielen einer Karte den anderen Eintrag betreffen – ein subtiler, schwer zu findender Fehler.

---

## Implementierung in CardSiege – Gegnersystem

Der `WaveSpawner` hält drei konfigurierte `EnemyPrototype`-Objekte, einen pro Gegnertyp. Die Attribute werden einmal pro Welle festgelegt und skalieren mit der Wellennummer:

```java
public void startWave(GameManager manager) {
    int currentWaveNumber = manager.getWavesCompleted() + 1;

    // Einmalig konfigurieren – Werte skalieren mit Welle
    int baseHp   = 5  + currentWaveNumber;
    int fastHp   = Math.max(3, 3 + currentWaveNumber / 2);
    int tankHp   = 11 + currentWaveNumber * 2;

    float baseSpeed = 54f + currentWaveNumber * 1.6f;
    float fastSpeed = 82f + currentWaveNumber * 2.2f;
    float tankSpeed = 38f + currentWaveNumber * 1.2f;

    standardEnemy = new EnemyPrototype(EnemyType.STANDARD, baseSpeed, baseHp);
    fastEnemy     = new EnemyPrototype(EnemyType.FAST,     fastSpeed, fastHp);
    tankEnemy     = new EnemyPrototype(EnemyType.TANK,     tankSpeed, tankHp);
}
```

Der `EnemyPrototype` selbst ist simpel – er hält nur Konfigurationsdaten und kann sich klonen:

```java
public class EnemyPrototype {
    private final EnemyType type;
    private final float speed;
    private final int hp;

    // copy() benötigt den aktuellen Pfad, da jeder Gegner seinen
    // eigenen, unabhängigen Fortschritt auf dem Pfad verfolgt
    public Enemy copy(List<Vector2> path) {
        return new Enemy(type, path, speed, hp);
    }
}
```

Beim Spawnen wird der passende Prototyp gewählt und geklont:

```java
public void update(GameManager manager, float delta) {
    timer += delta;
    if (timer >= spawnInterval && spawned < totalThisWave) {
        timer = 0f;
        // Prototyp auswählen und mit aktuellem Pfad klonen
        manager.spawnEnemy(
            selectPrototypeForIndex(spawned).copy(manager.getBoard().getPath())
        );
        spawned++;
    }
}

private EnemyPrototype selectPrototypeForIndex(int index) {
    if (currentWaveNumber >= 3 && index % 5 == 0) return tankEnemy;
    if (currentWaveNumber >= 2 && index % 3 == 0) return fastEnemy;
    return standardEnemy;
}
```

Jeder gespawnte `Enemy` hat seinen **eigenen** Pfadfortschritt (`pathIndex`), obwohl alle von denselben Prototypen stammen. Die Konfiguration (Geschwindigkeit, HP) ist zentralisiert, der Zustand (Position, verbleibende HP im Kampf) ist pro Instanz.

---

## Vergleich der beiden Anwendungsfälle

| Aspekt | Kartensystem | Gegnersystem |
|---|---|---|
| Wer klont? | `Deck.refill()` | `WaveSpawner.update()` |
| Warum klonen? | Jede Karte muss unabhängig ausgespielt werden können | Jeder Gegner hat eigenen Pfadfortschritt |
| Zusatzparameter bei `copy()`? | Nein | Ja: `path` (aktueller Spielfeldpfad) |
| Wie oft pro Prototyp? | 2x (festes Deck-Volumen) | Variabel (laut Wellenkonfiguration) |

---

## Zusammenspiel mit anderen Patterns

- **Factory Method:** Die Factory erzeugt die Prototypen und gibt sie als Liste zurück. Das `Deck` klont sie. Die Verantwortlichkeiten sind klar geteilt: Factory = Konfiguration, Prototype = Klonen.
- **Command:** Geklonte Karten sind sofort einsatzbereite Command-Objekte. Da `copy()` alle Parameter überträgt, ist jede Kopie vollständig konfiguriert und direkt ausführbar.
- **Builder:** Der `WaveSpawner` konfiguriert die Prototypen zu Wellenbeginn – ähnlich wie ein Builder, der Parameter sammelt. `copy()` ist dann die finale Erzeugung mit einem zusätzlichen Parameter (dem Pfad).

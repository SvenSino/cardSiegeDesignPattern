# Builder – PendingTower

**Kategorie:** Erzeugungsmuster
**Beteiligte Klassen:** `PendingTower`, `BaseTower`, `BuildTowerCard`, `GameManager`, `GameScreen`

---

## Allgemeine Erklärung

Das Builder-Pattern trennt die **schrittweise Konfiguration** eines komplexen Objekts von seiner endgültigen **Erzeugung**. Anstatt alle Parameter auf einmal im Konstruktor zu übergeben, sammelt ein Builder-Objekt die Informationen schrittweise – und erzeugt das fertige Objekt erst, wenn alle nötigen Daten vorhanden sind.

**Klassisches GoF-Builder-Muster** besteht aus:
- `Builder` (Interface mit `buildPartA()`, `buildPartB()`, ...)
- `ConcreteBuilder` (setzt die Teile zusammen)
- `Director` (ruft den Builder in der richtigen Reihenfolge auf)
- `Product` (das fertige Objekt)

**Variante in CardSiege:** Das Muster ist vereinfacht – es gibt keinen separaten Director. Der `PendingTower` übernimmt gleichzeitig die Rolle des Builders (sammelt Parameter) und des ConcreteBuilders (erzeugt das Produkt per `build()`). Das ist eine pragmatische, für dieses Problem gut passende Interpretation.

---

## Beteiligte Rollen (GoF, angepasst)

| Rolle | Beschreibung | Im Projekt |
|---|---|---|
| Builder / ConcreteBuilder | Hält alle Konfigurationsparameter, erzeugt das Produkt | `PendingTower` |
| Product | Das fertige Objekt | `BaseTower` |
| Director | Startet den Bauprozess in zwei Schritten | `BuildTowerCard` (Schritt 1), `GameManager` (Schritt 2) |

---

## Das Problem: zweistufige Erzeugung

Wenn der Spieler eine `BuildTowerCard` ausspielt, sind zu diesem Zeitpunkt noch nicht alle Parameter bekannt. Die Karte kennt Schaden, Reichweite, Cooldown und Strategien – aber **nicht** die Gitterkoordinaten, auf denen der Turm platziert werden soll. Die kennt nur der Spieler, wenn er auf ein Feld klickt.

Eine direkte Erzeugung von `BaseTower` beim Kartenspielen ist daher unmöglich. Der Prozess ist zwingend zweistufig:

1. **Karte ausspielen** → alle bekannten Parameter speichern
2. **Feld anklicken** → Koordinaten sind bekannt → Turm erzeugen

---

## Implementierung in CardSiege

### Schritt 1: Karte ausspielen – PendingTower befüllen

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
        // Alle bekannten Parameter in PendingTower einsammeln
        PendingTower pending = new PendingTower(
            getName(), type, getCost(), damage, range, cooldown,
            targeting, attackStrategy
        );
        // Beim Manager registrieren – GameScreen zeigt jetzt den Platzierungsmarker
        context.getManager().queueTowerPlacement(pending);
    }
}
```

### Der PendingTower – Parameter-Container mit build()-Methode

```java
public class PendingTower {
    private final String name;
    private final TowerType type;
    private final int cost;
    private final int baseDamage;
    private final float range;
    private final float cooldown;
    private final TargetingStrategy targeting;
    private final AttackStrategy attackStrategy;

    // Schritt 2: Koordinaten sind jetzt bekannt -> BaseTower erzeugen
    public TowerComponent build(int gridX, int gridY) {
        return new BaseTower(
            name, type, gridX, gridY, baseDamage,
            range, cooldown, cost, targeting, attackStrategy
        );
    }
}
```

### Schritt 2: Spieler klickt Zielfeld – GameManager baut den Turm

```java
public boolean tryPlacePendingTower(int gridX, int gridY) {
    if (pendingTower == null) return false;
    if (!board.isBuildable(gridX, gridY)) return false;

    // Prüfen ob Feld schon belegt
    for (TowerComponent tower : towers) {
        if (tower.getGridX() == gridX && tower.getGridY() == gridY) return false;
    }

    // Jetzt erst: build() aufrufen – alle Parameter vollständig
    TowerComponent tower = pendingTower.build(gridX, gridY);
    towers.add(tower);
    pendingTower = null;
    gold -= tower.getCost();
    eventBus.publish(GameEvent.towerBuilt(gridX, gridY));
    return true;
}
```

---

## Nebeneffekt: visueller Platzierungsmarker

Der `PendingTower` hat einen praktischen Nebennutzen, der direkt aus der Architekturentscheidung erwächst: Solange ein `PendingTower` aktiv ist (d.h. `pendingTower != null` im `GameManager`), rendert der `GameScreen` einen Platzierungsmarker mit Reichweitenkreis auf dem Spielfeld.

Der Spieler sieht, welchen Bereich der Turm abdecken würde – **bevor** er die endgültige Entscheidung trifft, und **ohne** dass dafür ein echter `BaseTower` existieren muss. Das Builder-Pattern liefert hier nicht nur sauberere Objekterzeugung, sondern ermöglicht direkt ein UI-Feature.

```
[Karte ausspielen]
      |
      v
  PendingTower erstellt
      |
      +---> GameScreen zeigt Platzierungsvorschau mit Reichweitenkreis
      |
      v
  [Spieler klickt gültiges Feld]
      |
      v
  pendingTower.build(x, y)
      |
      v
  BaseTower in Turmlist eingetragen
  PendingTower = null
  Vorschau verschwindet
```

---

## Zusammenspiel mit anderen Patterns

- **Command:** `BuildTowerCard.execute()` ist der Auslöser von Schritt 1. Die Karte kennt alle Turmparameter (aus der Factory-Konfiguration) und übergibt sie an `PendingTower`.
- **Factory Method:** Die `StrategyCardFactory` konfiguriert die `BuildTowerCard`-Objekte mit konkreten Werten und Strategien. Diese Konfiguration landet am Ende per Builder im fertigen `BaseTower`.
- **Prototype:** `BuildTowerCard.copy()` klont die Karte inklusive aller Parameter – auch die `TargetingStrategy`- und `AttackStrategy`-Referenzen werden übertragen.
- **Decorator:** Der fertige `BaseTower` (Produkt des Builders) kann später durch Decorators erweitert werden (`DamageBuffDecorator`, `RangeBuffDecorator`, `UpgradeDecorator`). Der Builder erzeugt immer den Kern – Decorators kommen nachträglich.

# Decorator – Turmerweiterungssystem

**Kategorie:** Strukturmuster
**Beteiligte Klassen:** `TowerComponent` (Interface), `TowerDecorator` (abstrakt), `BaseTower`, `DamageBuffDecorator`, `RangeBuffDecorator`, `UpgradeDecorator`

---

## Allgemeine Erklärung

Das Decorator-Pattern fügt einem Objekt **zur Laufzeit** zusätzliches Verhalten hinzu, ohne die Klasse zu verändern. Es funktioniert durch Hüllen (Wrapper): Ein Decorator implementiert dasselbe Interface wie das Objekt, das er umhüllt, hält eine Referenz auf dieses Objekt (`inner`) und delegiert alle Methoden daran weiter – bis auf jene, für die er einen Bonus hinzufügt.

**Das Hauptmerkmal:** Decorators sind **stapelbar**. Man kann beliebig viele Decorator-Schichten übereinanderlegen, und jeder Aufruf traversiert transparent die gesamte Kette.

**Alternative: Vererbung** – und warum sie hier scheitert:
Stellt man sich vor, Türme könnten Schadensbuff, Reichweitenbuff und ein Level-Up erhalten, ergibt das mit Vererbung:
- `BaseTower`
- `DamageBuff_BaseTower`
- `RangeBuff_BaseTower`
- `Upgrade_BaseTower`
- `DamageBuff_RangeBuff_BaseTower`
- `DamageBuff_Upgrade_BaseTower`
- `RangeBuff_Upgrade_BaseTower`
- `DamageBuff_RangeBuff_Upgrade_BaseTower`

Das sind bereits 8 Klassen für 3 Turmtypen × 3 Bufftypen = **24 Klassen**, die bei jedem neuen Bufftyp exponentiell wachsen. Mit dem Decorator-Pattern: weiterhin eine Klasse pro Bufftyp, beliebig kombinierbar.

---

## Beteiligte Rollen (GoF)

| Rolle | Beschreibung | Im Projekt |
|---|---|---|
| Component | Gemeinsames Interface | `TowerComponent` |
| ConcreteComponent | Das Basisobjekt ohne Erweiterungen | `BaseTower` |
| Decorator | Abstrakte Basisklasse, delegiert alle Methoden | `TowerDecorator` |
| ConcreteDecorator | Überschreibt spezifische Methoden | `DamageBuffDecorator`, `RangeBuffDecorator`, `UpgradeDecorator` |

---

## Implementierung in CardSiege

### Das gemeinsame Interface

```java
public interface TowerComponent {
    int getGridX();
    int getGridY();
    String getName();
    TowerType getType();
    int getLevel();
    int getDamage();
    float getRange();
    float getCooldown();
    int getCost();
    TargetingStrategy getTargetingStrategy();
    AttackStrategy getAttackStrategy();
    Set<Enemy> getEnemiesInRange();
    void onEnemyEnteredRange(Enemy enemy);
    void onEnemyExitedRange(Enemy enemy);
    boolean canFire();
    void resetCooldown();
    void tickCooldown(float delta);
    void update(float delta);
}
```

Sowohl `BaseTower` als auch alle Decorators implementieren dieses Interface. Die restliche Spiellogik kennt nur `TowerComponent` – sie sieht nie einen konkreten Typ.

### Die abstrakte Decorator-Basisklasse

```java
public abstract class TowerDecorator implements TowerComponent {
    protected final TowerComponent inner;  // das umhüllte Objekt

    protected TowerDecorator(TowerComponent inner) {
        this.inner = inner;
    }

    // Alle Methoden delegieren standardmäßig an inner
    @Override public int getDamage()   { return inner.getDamage(); }
    @Override public float getRange()  { return inner.getRange(); }
    @Override public int getLevel()    { return inner.getLevel(); }
    @Override public float getCooldown() { return inner.getCooldown(); }
    // ... alle anderen Methoden ebenso
}
```

`TowerDecorator` ist der Kern des Patterns: Er implementiert das gesamte Interface durch einfache Delegation. Konkrete Decorators erben diese Delegation und überschreiben nur das, was sie ändern wollen.

### Die drei konkreten Decorators

**DamageBuffDecorator** – erhöht den Schaden um einen festen Bonus:

```java
public class DamageBuffDecorator extends TowerDecorator {
    private final int bonusDamage;

    public DamageBuffDecorator(TowerComponent inner, int bonusDamage) {
        super(inner);
        this.bonusDamage = bonusDamage;
    }

    @Override
    public int getDamage() {
        return inner.getDamage() + bonusDamage;  // addiert auf den Wert darunter
    }
    // alle anderen Methoden bleiben delegiert
}
```

**RangeBuffDecorator** – erhöht die Reichweite:

```java
public class RangeBuffDecorator extends TowerDecorator {
    private final float bonusRange;

    public RangeBuffDecorator(TowerComponent inner, float bonusRange) {
        super(inner);
        this.bonusRange = bonusRange;
    }

    @Override
    public float getRange() {
        return inner.getRange() + bonusRange;
    }
}
```

**UpgradeDecorator** – erhöht gleichzeitig Level, Schaden und Reichweite, reduziert Cooldown:

```java
public class UpgradeDecorator extends TowerDecorator {
    private final int bonusDamage;       // +1
    private final float bonusRange;      // +15f
    private final float cooldownReduction; // -0.05f

    @Override public int getDamage()   { return inner.getDamage() + bonusDamage; }
    @Override public float getRange()  { return inner.getRange() + bonusRange; }
    @Override public int getLevel()    { return inner.getLevel() + 1; }

    @Override
    public float getCooldown() {
        // Minimum-Cooldown von 0.2s verhindert unendlich schnelles Feuern
        return Math.max(0.2f, inner.getCooldown() - cooldownReduction);
    }
}
```

---

## Stapelbarkeit – wie sie im Spiel entsteht

Angenommen, ein Scout-Tower wird mit zwei Schadensbuffs und einem Level-Up belegt:

```
UpgradeDecorator
    └── DamageBuffDecorator (bonusDamage=1)
            └── DamageBuffDecorator (bonusDamage=1)
                    └── BaseTower (damage=2)
```

Ein Aufruf von `getDamage()` traversiert die gesamte Kette:

```
UpgradeDecorator.getDamage()
  → inner.getDamage() + 1
    = DamageBuffDecorator.getDamage() + 1
      → inner.getDamage() + 1
        = DamageBuffDecorator.getDamage() + 1
          → inner.getDamage() + 1
            = BaseTower.getDamage()
            = 2
          = 2 + 1 = 3
        = 3 + 1 = 4
      = 4 + 1 = 5
```

Ergebnis: 5 Schadenspunkte. Die Spiellogik sieht nur `tower.getDamage()` und erhält transparent den akkumulierten Wert.

### Wie Buffs im GameManager angewendet werden

```java
// Globaler Schadensbuff – alle Türme erhalten einen Wrapper
public void applyDamageBuff(int bonusDamage) {
    applyTowerModifier(tower -> new DamageBuffDecorator(tower, bonusDamage));
}

// Gezielter Reichweitenbuff – wird beim nächsten Klick auf einen Turm angewendet
public void queueRangeBuff(float bonusRange) {
    queueTowerModifier(tower -> new RangeBuffDecorator(tower, bonusRange));
}

// Level-Up – ersetzt den Turm durch einen UpgradeDecorator
public TowerComponent upgradeTower(TowerComponent tower) {
    TowerComponent upgraded = new UpgradeDecorator(tower, 1, 15f, 0.05f);
    int index = towers.indexOf(tower);
    towers.set(index, upgraded);  // Referenz in der Liste ersetzen
    return upgraded;
}
```

Die Turmreferenz in der `towers`-Liste wird durch den Decorator ersetzt. Alles andere – `resolveCombat`, `updateRangeObservers`, `GameScreen`-Rendering – arbeitet weiterhin mit `TowerComponent` und bemerkt den Wechsel nicht.

---

## Zusammenspiel mit anderen Patterns

- **Command:** `DamageBuffCard.execute()` ruft `manager.applyDamageBuff()` auf – die einzige Stelle, an der `DamageBuffDecorator`-Objekte erzeugt werden. Die Karte weiß, *dass* ein Buff stattfindet, nicht *wie* er durch Wrapper realisiert wird.
- **Strategy:** Wenn `SplashAttackStrategy.attack()` `tower.getDamage()` aufruft, traversiert dieser Aufruf transparent die gesamte Decorator-Kette. Die Strategie erhält den kumulierten, gebufften Wert – ohne es zu wissen.
- **Builder:** Der `BaseTower` (Produkt des Builders) ist immer der Kern der Decorator-Kette. Builder und Decorator sind komplementär: Builder erzeugt das Basisobjekt, Decorators erweitern es nachträglich.
- **State:** Während der `WavePhase` können weiterhin Buff-Karten gespielt werden. Decorators werden also auch während einer laufenden Welle auf die Turmreferenzen in der `towers`-Liste angewendet.

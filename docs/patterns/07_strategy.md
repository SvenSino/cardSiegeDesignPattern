# Strategy – Zielwahl und Angriffsverhalten

**Kategorie:** Verhaltensmuster
**Beteiligte Klassen:** `TargetingStrategy`, `AttackStrategy`, `ClosestToGoalStrategy`, `HighestHpStrategy`, `SingleTargetAttackStrategy`, `SplashAttackStrategy`, `BaseTower`, `StrategyCardFactory`

---

## Allgemeine Erklärung

Das Strategy-Pattern definiert eine **Familie austauschbarer Algorithmen**, kapselt jeden in einer eigenen Klasse und macht sie untereinander austauschbar. Das Objekt, das den Algorithmus verwendet, delegiert die Ausführung vollständig an die Strategie-Instanz.

**Kerngedanke:** Das *Was* (der Algorithmus) wird vom *Wer* (das Objekt, das ihn braucht) getrennt. Statt `if (type == SCOUT) { ... } else if (type == SNIPER) { ... }` im Turm zu haben, kennt der Turm nur ein Interface und arbeitet gegen das.

**Wann sinnvoll:**
- Mehrere Varianten desselben Verhaltens existieren
- Die Varianten sind unabhängig von der Klasse, die sie nutzt
- Neue Varianten sollen ohne Änderung bestehender Klassen hinzukommen

---

## Beteiligte Rollen (GoF)

| Rolle | Beschreibung | Im Projekt (Zielwahl) | Im Projekt (Angriff) |
|---|---|---|---|
| Strategy | Interface des Algorithmus | `TargetingStrategy` | `AttackStrategy` |
| ConcreteStrategy | Konkrete Implementierung | `ClosestToGoalStrategy`, `HighestHpStrategy` | `SingleTargetAttackStrategy`, `SplashAttackStrategy` |
| Context | Nutzt die Strategie | `BaseTower` (via `TowerComponent`) | `BaseTower` (via `TowerComponent`) |

---

## Zwei orthogonale Dimensionen

Türme unterscheiden sich in zwei **unabhängigen** Dimensionen:

- **Zielwahl** (*Targeting*): Wen greife ich an?
- **Angriffstyp** (*Attack*): Wie greife ich an?

Diese Dimensionen sind orthogonal – jede Kombination ist denkbar. Deshalb gibt es zwei separate Strategy-Interfaces statt eines:

```java
// Dimension 1: Zielwahl
public interface TargetingStrategy {
    Enemy selectTarget(TowerComponent tower, List<Enemy> enemies);
}

// Dimension 2: Angriffsausführung
public interface AttackStrategy {
    Enemy attack(TowerComponent tower);
}
```

---

## Die konkreten Strategien

### Zielwahl-Strategien

**ClosestToGoalStrategy** – priorisiert den Gegner, der am weitesten auf dem Pfad fortgeschritten ist:

```java
public class ClosestToGoalStrategy implements TargetingStrategy {
    @Override
    public Enemy selectTarget(TowerComponent tower, List<Enemy> enemies) {
        return enemies.stream()
            .filter(enemy -> enemy.isInRange(tower))
            .max(Comparator.comparingInt(Enemy::getPathIndex))
            .orElse(null);
    }
}
```

`pathIndex` gibt an, welchen Wegpunkt ein Gegner zuletzt erreicht hat. Höherer Index = näher am Ziel = gefährlicher.

**HighestHpStrategy** – priorisiert den Gegner mit den meisten Lebenspunkten:

```java
public class HighestHpStrategy implements TargetingStrategy {
    @Override
    public Enemy selectTarget(TowerComponent tower, List<Enemy> enemies) {
        return enemies.stream()
            .filter(enemy -> enemy.isInRange(tower))
            .max(Comparator.comparingInt(Enemy::getHp))
            .orElse(null);
    }
}
```

Sinnvoll für den Sniper: Er soll starke Gegner eliminieren, bevor sie zu viel Schaden verursachen.

### Angriffs-Strategien

**SingleTargetAttackStrategy** – greift genau einen Gegner an:

```java
public class SingleTargetAttackStrategy implements AttackStrategy {
    @Override
    public Enemy attack(TowerComponent tower) {
        List<Enemy> inRange = new ArrayList<>(tower.getEnemiesInRange());
        if (inRange.isEmpty()) return null;

        // Zielwahl-Strategie bestimmt das Ziel
        Enemy target = tower.getTargetingStrategy().selectTarget(tower, inRange);
        if (target == null) return null;

        target.takeDamage(tower.getDamage());
        return target;
    }
}
```

Bemerkenswert: `SingleTargetAttackStrategy` ruft intern `tower.getTargetingStrategy().selectTarget()` auf – die `AttackStrategy` **delegiert die Zielwahl** an die `TargetingStrategy`. Die beiden Interfaces kooperieren.

**SplashAttackStrategy** – trifft primäres Ziel mit vollem Schaden, alle nahegelegenen Gegner mit reduziertem Schaden:

```java
public class SplashAttackStrategy implements AttackStrategy {
    private final float splashRadius;   // 36f pixel
    private final float splashFactor;   // 0.6f = 60% des vollen Schadens

    @Override
    public Enemy attack(TowerComponent tower) {
        List<Enemy> inRange = new ArrayList<>(tower.getEnemiesInRange());
        if (inRange.isEmpty()) return null;

        // Primärziel über Targeting-Strategie bestimmen
        Enemy primary = tower.getTargetingStrategy().selectTarget(tower, inRange);
        if (primary == null) return null;

        // Primärziel: voller Schaden
        primary.takeDamage(tower.getDamage());

        // Splash-Schaden: 60% auf alle Gegner im Umkreis
        int splashDamage = Math.max(1, Math.round(tower.getDamage() * splashFactor));
        float radiusSq = splashRadius * splashRadius;

        for (Enemy enemy : inRange) {
            if (enemy == primary) continue;
            float dx = enemy.getPosition().x - primary.getPosition().x;
            float dy = enemy.getPosition().y - primary.getPosition().y;
            if ((dx * dx + dy * dy) <= radiusSq) {
                enemy.takeDamage(splashDamage);
            }
        }
        return primary;
    }
}
```

`tower.getDamage()` traversiert dabei transparent die Decorator-Kette – die Strategie erhält immer den gebufften Schadenswert.

---

## Zuordnung der Strategien zu Turmtypen

Die `StrategyCardFactory` verdrahtet die Kombinationen:

```java
// Scout: greift den fortgeschrittensten Gegner mit Einzelschuss an
new BuildTowerCard("Scout Tower", 2, 2, 120f, 0.8f,
    new ClosestToGoalStrategy(),
    new SingleTargetAttackStrategy(),
    TowerType.SCOUT);

// Sniper: greift den stärksten Gegner mit Einzelschuss an
new BuildTowerCard("Sniper Tower", 3, 4, 180f, 1.4f,
    new HighestHpStrategy(),
    new SingleTargetAttackStrategy(),
    TowerType.SNIPER);

// Rapid: greift den fortgeschrittensten Gegner mit Flächenschaden an
new BuildTowerCard("Rapid Tower", 2, 1, 100f, 0.4f,
    new ClosestToGoalStrategy(),
    new SplashAttackStrategy(36f, 0.6f),
    TowerType.RAPID);
```

| Turmtyp | Targeting | Attack | Stärke |
|---|---|---|---|
| Scout | ClosestToGoal | SingleTarget | Mittel (DMG 2), schnell (0.8s) |
| Sniper | HighestHp | SingleTarget | Stark (DMG 4), langsam (1.4s) |
| Rapid | ClosestToGoal | Splash | Schwach (DMG 1), sehr schnell (0.4s) |

---

## Nutzung im GameManager (Kampfauflösung)

```java
private void resolveCombat(float delta) {
    for (TowerComponent tower : towers) {
        if (tower.canFire()) {
            // Strategie ausführen – GameManager weiß nicht welche
            Enemy target = tower.getAttackStrategy().attack(tower);
            if (target != null) {
                tower.resetCooldown();
                spawnProjectile(tower, target);
            }
        } else {
            tower.tickCooldown(delta);
        }
    }
}
```

`GameManager` kennt keine konkreten Strategie-Klassen. Er ruft `getAttackStrategy().attack(tower)` auf und erhält ein Ergebnis-Objekt (`Enemy`) zurück – oder `null` wenn kein Ziel verfügbar war.

---

## Zusammenspiel mit anderen Patterns

- **Decorator:** `tower.getDamage()` in der `SplashAttackStrategy` traversiert transparent die Decorator-Kette. Ein gebuffter Turm (`DamageBuffDecorator(BaseTower)`) liefert automatisch den korrekten, erhöhten Schadenswert – ohne dass die Strategie es weiß.
- **Builder:** Die Strategie-Instanzen werden in `BuildTowerCard` gespeichert und per `PendingTower.build()` in den `BaseTower` übertragen. Die Factory wählt die Kombination, der Builder übergibt sie.
- **Command:** `BuildTowerCard.execute()` erzeugt einen `PendingTower` mit den konfigurierten Strategie-Instanzen. Die Karte trägt die Strategien als ihre Parameter – sie sind Teil des Command-Objekts.
- **Factory Method:** `StrategyCardFactory` ist der Ort, an dem Strategie-Kombinationen entschieden werden. Neue Kombinationen (z.B. `HighestHp + Splash`) erfordern keine Änderung im Turm-Code – nur eine neue `BuildTowerCard`-Konfiguration in der Factory.

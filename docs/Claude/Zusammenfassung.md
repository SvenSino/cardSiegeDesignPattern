# CardSiege – Dokumentations-Zusammenfassung

Diese Datei fasst alle anderen Dateien im `docs/Claude/`-Ordner zusammen und ergänzt sie um eine Beschreibung der Gegnererzeugung und des Targeting-Systems.

---

## 1. Composite Pattern – Board/Tile/BoardComponent

*(Quelle: Composite.md)*

### Struktur im Projekt

Das Projekt hat bereits ein `BoardComponent`-Interface, das sowohl `Board` als auch `Tile` implementieren. Das sieht aus wie ein Composite-Pattern – ist es aber nur halb.

```
BoardComponent  (Interface)
├── Tile        (Blatt / Leaf)
└── Board       (Composite)
```

### Warum es kein echtes Composite ist

Das Composite-Pattern macht Sinn, wenn Leaf und Composite **uniform behandelt** werden – also wenn irgendwo eine `List<BoardComponent>` existiert, die mal ein `Tile` und mal ein ganzes `Board` enthält, und die aufrufende Stelle den Unterschied nicht kennen muss.

Das passiert hier nie. `Board` und `Tile` werden immer getrennt angesprochen:
- `board.getTiles()` liefert explizit `List<Tile>`
- `board.isBuildable(x, y)` ist nur auf `Board` sinnvoll, nicht auf `Tile`
- Niemand hat eine `List<BoardComponent>` mit gemischten Inhalten

Das Interface `BoardComponent` existiert, wird aber faktisch nicht als Composite genutzt. Es ist eher ein strukturelles Relikt als ein bewusst eingesetztes Pattern.

### Wie echtes Composite hier aussehen würde

```java
interface BoardComponent {
    boolean isPassable();
    void render(SpriteBatch batch);
}

class Tile implements BoardComponent {       // Blatt
    @Override
    public boolean isPassable() { return !path; }
}

class Board implements BoardComponent {      // Composite
    private final List<Tile> tiles;

    @Override
    public boolean isPassable() {
        return tiles.stream().anyMatch(Tile::isPassable);
    }

    @Override
    public void render(SpriteBatch batch) {
        tiles.forEach(t -> t.render(batch)); // delegiert an Kinder
    }
}
```

Dann könnte man z.B. `List<BoardComponent>` schreiben und darin sowohl einzelne Tiles als auch ganze Boards ablegen – das ist die eigentliche Stärke des Patterns.

### Ehrlichere Alternative: Enemy-Squads

Konzeptionell wäre Composite bei Gegnern sinnvoller, weil es ein echtes Problem löst:

```java
interface EnemyComponent {
    void takeDamage(int amount);
    boolean isDefeated();
}

class Enemy implements EnemyComponent { ... }        // Blatt

class EnemySquad implements EnemyComponent {         // Composite
    private final List<EnemyComponent> members;

    @Override
    public void takeDamage(int amount) {
        members.forEach(m -> m.takeDamage(amount));  // AoE trivial
    }

    @Override
    public boolean isDefeated() {
        return members.stream().allMatch(EnemyComponent::isDefeated);
    }
}
```

`SplashAttackStrategy` müsste dann nicht mehr manuell alle Gegner im Radius iterieren – `squad.takeDamage(splash)` reicht.

### Fazit

| | Board/Tile | Enemy-Squads |
|---|---|---|
| Struktur im Projekt | bereits angedeutet | komplett flach |
| Echter Mehrwert | gering | real (AoE, Wellen) |
| Uniform behandelt? | nein | ja |

Das Board-Composite ist technisch korrekt aufgebaut, aber akademisch – der Kernvorteil des Patterns (uniforme Behandlung) materialisiert sich im Code nicht. Enemy-Squads wären die ehrlichere Anwendung.

---

## 2. Notizen und Erklärungen zu Design-Entscheidungen

*(Quelle: Dinge.md)*

### Decorator-Kette und Strategy-Entkopplung

`SplashAttackStrategy` ruft `tower.getDamage()` auf einem `TowerComponent` auf. Dieses `tower` könnte in Wirklichkeit ein `DamageBuffDecorator(UpgradeDecorator(BaseTower))` sein — aber das sieht die Strategie nicht, weil alle Dekoratoren ebenfalls `TowerComponent` implementieren. Der Aufruf traversiert die Kette automatisch und gibt den summierten Schadenswert zurück.

Das funktioniert nur, weil Strategy (`AttackStrategy`) und Decorator (`TowerDecorator`) beide gegen dasselbe Interface `TowerComponent` programmiert sind — die Strategie kann gar nicht unterscheiden, ob sie einen nackten Turm oder eine Kette von Wrappern vor sich hat.

Kurz: Möglich ist das, weil Strategy und Decorator dasselbe Interface `TowerComponent` teilen.

### Factory und die „eine Quelle der Wahrheit"

Der Einsatz desselben Factory-Patterns an zwei unabhängigen Stellen illustriert seine Vielseitigkeit. In beiden Fällen liegt der Kern des Problems nicht in der Erzeugung an sich, sondern in der zentralen Verwaltung von Konfiguration. Es soll eine einzige, verlässliche Quelle der Wahrheit für die Eigenschaften eines Objekttyps geben.

**Karten:** Die `StrategyCardFactory` legt einmal fest, wie eine `DamageBuffCard` aussieht — welchen Bonuswert sie hat, was sie kostet usw. Das Deck klont davon Kopien, aber die Konfiguration selbst steht nur an einer Stelle. Willst du den Schadenswert ändern, änderst du es in der Factory — nicht an jedem Ort, wo eine Karte entsteht.

**Gegner:** Der `WaveSpawner` legt einmal pro Welle fest, wie ein `StandardEnemy` dieser Welle aussieht — Geschwindigkeit, HP, skaliert mit der Wellennummer. Das ist wieder die eine Quelle der Wahrheit. Jeder einzelne Spawn klont davon, aber die Werte stehen nur im Prototyp.

Das eigentliche Problem ist nicht „ich brauche mehrere Instanzen" (das wäre trivial mit `new`), sondern „ich will die Eigenschaften eines Typs an genau einer Stelle definieren und nicht bei jeder Erzeugung wiederholen".

---

## 3. Gegnererzeugung

### Enemy-Klasse

Die Klasse `Enemy` (`core/src/main/java/td/core/model/enemies/Enemy.java`) repräsentiert einen einzelnen Gegner. Ein Gegner folgt einer vorberechneten Liste von Wegpunkten (`List<Vector2>`), trackt seinen Fortschritt über einen `pathIndex` und bewegt sich pro Frame einen Schritt weiter.

Wichtige Eigenschaften:
- `speed` und `hp` bestimmen Geschwindigkeit und Lebenspunkte
- `slowTimer` / `slowFactor` ermöglichen Verlangsamungseffekte durch Türme
- `reachedGoal` wird `true`, sobald der letzte Wegpunkt erreicht ist

Bewegungslogik: Der Gegner berechnet pro `update(delta)` die Richtung zum nächsten Waypoint, normiert den Vektor und bewegt sich um `speed * slowFactor * delta` Einheiten. Ist der Waypoint nah genug, wird `pathIndex` erhöht.

### EnemyType

```java
public enum EnemyType {
    STANDARD,  // Rot  – normal ausgewogen
    FAST,      // Gelb – schnell, wenig HP
    TANK       // Lila – langsam, viel HP
}
```

### Prototype-Pattern für Gegner

`EnemyPrototype` (`core/src/main/java/td/core/model/enemies/EnemyPrototype.java`) speichert die Konfiguration (Typ, Geschwindigkeit, HP) eines Gegnertyps. Per `copy(path)` wird daraus eine konkrete `Enemy`-Instanz erzeugt:

```java
public Enemy copy(List<Vector2> path) {
    return new Enemy(type, path, speed, hp);
}
```

Das ist das **Prototype-Pattern**: Die Konfiguration steht einmal im Prototyp, jeder Spawn ist eine Kopie davon. Werte müssen nicht bei jedem `new Enemy(...)` wiederholt werden.

### WaveSpawner

`WaveSpawner` (`core/src/main/java/td/core/model/game/WaveSpawner.java`) steuert, wann und welche Gegner gespawnt werden.

**Beim Wellenstart** (`startWave`) werden drei `EnemyPrototype`-Objekte erzeugt, deren Werte mit der Wellennummer skalieren:

| Eigenschaft | Formel |
|---|---|
| Gegner pro Welle | `min(28, 8 + welle * 2)` |
| Spawn-Intervall | `max(0.32s, 0.75s - welle * 0.03s)` |
| Standard-HP | `5 + welle` |
| Fast-HP | `max(3, 3 + welle / 2)` |
| Tank-HP | `11 + welle * 2` |
| Standard-Speed | `54 + welle * 1.6` |
| Fast-Speed | `82 + welle * 2.2` |
| Tank-Speed | `38 + welle * 1.2` |

**Pro Frame** (`update`) zählt ein Timer hoch. Ist er größer als das Spawn-Intervall und wurden noch nicht alle Gegner der Welle gespawnt, wird ein Gegner gespawnt und der Timer zurückgesetzt.

**Welcher Gegnertyp** wird über `selectPrototypeForIndex(index)` bestimmt:
- Ab Welle 3: Jeder 5. Gegner ist ein Tank
- Ab Welle 2: Jeder 3. Gegner ist ein Fast-Enemy
- Sonst: Standard-Enemy

```java
private EnemyPrototype selectPrototypeForIndex(int index) {
    if (currentWaveNumber >= 3 && index % 5 == 0) return tankEnemy;
    if (currentWaveNumber >= 2 && index % 3 == 0) return fastEnemy;
    return standardEnemy;
}
```

### Spawn-Ablauf (komplett)

```
WaveSpawner.update(delta)
  -> selectPrototypeForIndex(spawned) wählt Prototyp
  -> EnemyPrototype.copy(path) erzeugt Enemy-Instanz
  -> GameManager.spawnEnemy(enemy) fügt Enemy zur Liste hinzu
```

---

## 4. Targeting-System

### Überblick: Zwei Strategy-Ebenen

Das Targeting ist in zwei unabhängige Strategies aufgeteilt:

1. **`TargetingStrategy`** – *Wen* greift der Turm an?
2. **`AttackStrategy`** – *Wie* greift der Turm an?

Beide arbeiten gegen das `TowerComponent`-Interface, weshalb sie mit nackten Türmen und Decorator-Ketten gleich funktionieren.

### Range-Tracking: Observer-Mechanismus

Der `GameManager` prüft in jedem Frame (`updateRangeObservers()`), welche Gegner im Radius jedes Turms liegen:

1. Für jedes Turm-Gegner-Paar wird `enemy.isInRange(tower)` geprüft (Distanz-Formel: `dx² + dy² <= range²`)
2. Neu in den Radius eingetretene Gegner → `tower.onEnemyEnteredRange(enemy)`
3. Den Radius verlassende Gegner → `tower.onEnemyExitedRange(enemy)`

Der Turm hält intern ein `Set<Enemy> enemiesInRange` aktuell. Die `AttackStrategy` greift darauf zu, ohne selbst Distanzen berechnen zu müssen.

### TargetingStrategy – Zielwahl

**`ClosestToGoalStrategy`** wählt den Gegner mit dem höchsten `pathIndex` – also den, der am weitesten auf dem Pfad vorangeschritten ist (am nächsten am Ziel):

```java
return enemies.stream()
    .filter(enemy -> enemy.isInRange(tower))
    .max(Comparator.comparingInt(Enemy::getPathIndex))
    .orElse(null);
```

**`HighestHpStrategy`** wählt den Gegner mit den meisten verbleibenden HP:

```java
return enemies.stream()
    .filter(enemy -> enemy.isInRange(tower))
    .max(Comparator.comparingInt(Enemy::getHp))
    .orElse(null);
```

Beide filtern zuerst nach Reichweite und geben `null` zurück, wenn kein Ziel vorhanden ist.

### AttackStrategy – Angriffsverhalten

**`SingleTargetAttackStrategy`:**
1. Holt `enemiesInRange` vom Turm
2. Ruft `targetingStrategy.selectTarget()` auf
3. Wendet `tower.getDamage()` auf das Ziel an

**`SplashAttackStrategy`:**
1. Wählt Primärziel per `TargetingStrategy`
2. Volles `getDamage()` auf das Primärziel
3. Reduzierter Schaden (`getDamage() * splashFactor`, min. 1) auf alle anderen Gegner im `splashRadius` um das Primärziel

```java
int splashDamage = Math.max(1, Math.round(tower.getDamage() * splashFactor));
float radiusSq = splashRadius * splashRadius;
for (Enemy enemy : inRange) {
    if (enemy == primary) continue;
    float dx = enemy.getPosition().x - primary.getPosition().x;
    float dy = enemy.getPosition().y - primary.getPosition().y;
    if ((dx * dx + dy * dy) <= radiusSq) enemy.takeDamage(splashDamage);
}
```

### Combat-Loop im GameManager

`resolveCombat(delta)` läuft jeden Frame:

```java
for (TowerComponent tower : towers) {
    if (tower.canFire()) {
        Enemy target = tower.getAttackStrategy().attack(tower);
        if (target != null) {
            tower.resetCooldown();
            spawnProjectile(tower, target);
        }
    } else {
        tower.tickCooldown(delta);
    }
}
```

Kann ein Turm feuern, delegiert er an die `AttackStrategy`. Trifft diese ein Ziel, wird der Cooldown zurückgesetzt und ein visuelles Projektil erzeugt.

### Vollständiger Update-Zyklus

```
GameManager.update(delta)
  1. phase.update()             – Spielphasen-Logik
  2. waveSpawner.update()       – Gegner spawnen
  3. enemy.update() (alle)      – Gegner bewegen
  4. updateRangeObservers()     – Reichweiten tracken
  5. tower.update() (alle)      – Türme updaten
  6. resolveCombat()            – Angriffe auflösen
  7. cleanupEntities()          – Tote/entkam entfernen
  8. updateProjectiles()        – Visuelle Projektile
```

### Zusammenspiel aller Beteiligten

```
resolveCombat()
  -> tower.getAttackStrategy().attack(tower)
       -> tower.getEnemiesInRange()          (vom Observer gepflegt)
       -> tower.getTargetingStrategy()
            .selectTarget(tower, enemies)    (filtert & wählt)
       -> target.takeDamage(tower.getDamage()) (traversiert Decorator-Kette)
```

`tower.getDamage()` kann dabei `DamageBuffDecorator(UpgradeDecorator(BaseTower)).getDamage()` traversieren — die `AttackStrategy` merkt davon nichts, weil alle Schichten `TowerComponent` implementieren.

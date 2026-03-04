# Composite Pattern – Board/Tile/BoardComponent

## Struktur im Projekt

Das Projekt hat bereits ein `BoardComponent`-Interface, das sowohl `Board` als auch `Tile` implementieren. Das sieht aus wie ein Composite-Pattern – ist es aber nur halb.

```
BoardComponent  (Interface)
├── Tile        (Blatt / Leaf)
└── Board       (Composite)
```

## Warum es kein echtes Composite ist

Das Composite-Pattern macht Sinn, wenn Leaf und Composite **uniform behandelt** werden – also wenn irgendwo eine `List<BoardComponent>` existiert, die mal ein `Tile` und mal ein ganzes `Board` enthält, und die aufrufende Stelle den Unterschied nicht kennen muss.

Das passiert hier nie. `Board` und `Tile` werden immer getrennt angesprochen:
- `board.getTiles()` liefert explizit `List<Tile>`
- `board.isBuildable(x, y)` ist nur auf `Board` sinnvoll, nicht auf `Tile`
- Niemand hat eine `List<BoardComponent>` mit gemischten Inhalten

Das Interface `BoardComponent` existiert, wird aber faktisch nicht als Composite genutzt. Es ist eher ein strukturelles Relikt als ein bewusst eingesetztes Pattern.

## Wie echtes Composite hier aussehen würde

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

## Ehrlichere Alternative: Enemy-Squads

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

## Fazit

| | Board/Tile | Enemy-Squads |
|---|---|---|
| Struktur im Projekt | bereits angedeutet | komplett flach |
| Echter Mehrwert | gering | real (AoE, Wellen) |
| Uniform behandelt? | nein | ja |

Das Board-Composite ist technisch korrekt aufgebaut, aber akademisch – der Kernvorteil des Patterns (uniforme Behandlung) materialisiert sich im Code nicht. Enemy-Squads wären die ehrlichere Anwendung.

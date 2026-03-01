# 🗼 Card Siege

> Kartenbasiertes Tower-Defense-Spiel als Showcase für 9 klassische GoF Design Patterns – gebaut mit Java & libGDX.

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![libGDX](https://img.shields.io/badge/libGDX-1.x-red)
![Gradle](https://img.shields.io/badge/Gradle-9.3-blue?logo=gradle)
![Patterns](https://img.shields.io/badge/Design%20Patterns-9%20GoF-green)
![Lombok](https://img.shields.io/badge/Lombok-✓-yellow)

---

## Inhaltsverzeichnis

- [Konzept](#konzept)
- [Spielmechaniken](#spielmechaniken)
- [Türme](#türme)
- [Karten](#karten)
- [Gegner](#gegner)
- [Spielphasen](#spielphasen)
- [Design-Pattern-Architektur](#design-pattern-architektur)
- [Paketstruktur](#paketstruktur)
- [Steuerung](#steuerung)
- [Technologien](#technologien)
- [Setup & Installation](#setup--installation)

---

## Konzept

**TD-DesignPattern** ist ein **kartenbasiertes Tower-Defense-Spiel**, in dem der Spieler auf einem **20×20-Raster** Türme baut, Gegner-Wellen abwehrt und eine Basis mit 10 HP verteidigt. Türme werden nicht direkt gekauft, sondern über ein **Kartensystem** gespielt – jede Karte kapselt eine Aktion (Command Pattern). Über **12 Wellen** werden die Gegner zunehmend stärker.

Das Projekt ist bewusst als **Design-Pattern-Showcase** konzipiert: Jede spielmechanische Entscheidung spiegelt den Einsatz eines GoF-Patterns wider.

---

## Spielmechaniken

| Feature | Details |
|---|---|
| **Kartensystem** | 3 Karten pro Zug ziehen, Energie verwalten, Karten mit `[1-8]` ausspielen |
| **Turmbau** | Karte spielen → Platzierungsmarker erscheint → Klick auf freies Feld |
| **Turm-Upgrades** | Turm auswählen + `[U]` drücken (kostet Gold) |
| **Buff-Karten** | Schaden, Reichweite – stapelbar auf bestehenden Türmen (Decorator) |
| **Wellen-System** | 12 Wellen, Gegner werden pro Welle stärker (mehr HP, schneller, mehr Einheiten) |
| **Rundenstruktur** | Draw → Play → Wave → Resolve → (nächste Runde) |
| **Siegbedingung** | Alle 12 Wellen abwehren |
| **Niederlage** | Basis-HP sinkt auf 0 (jeder durchgelassene Gegner kostet 1 HP) |
| **Modular & Erweiterbar** | Neue Karten, Türme oder Gegner-Typen ohne Änderungen am Core-Code |

---

## Türme

Drei Turm-Typen, jeweils mit eigener **Targeting-** und **Angriffs-Strategie**:

| Turm | Kosten (E / G) | DMG | RNG | CD | Targeting | Angriff |
|---|:---:|:---:|:---:|:---:|---|---|
| **Scout Tower** | 2E / 2G | 2 | 120 | 0.8s | Vorderster Gegner | Einzelziel |
| **Sniper Tower** | 3E / 3G | 4 | 180 | 1.4s | Höchste HP | Einzelziel |
| **Rapid Tower** | 2E / 2G | 1 | 100 | 0.4s | Vorderster Gegner | Splash (60% im 36px-Radius) |

> **Upgrades:** Jeder Turm kann manuell geupgradet werden (`[U]`). Upgrade-Kosten: `2 + Level` Gold.
> **Buff-Karten** stapeln `DamageBuffDecorator`, `RangeBuffDecorator` und `UpgradeDecorator` dynamisch auf dem Turm-Objekt.

---

## Karten

Das Deck enthält **10 einzigartige Karten-Typen**, je 2 Exemplare im Stapel (20 Karten gesamt). Nach dem Leerspielen wird das Deck automatisch nachgefüllt und gemischt.

| Karte | Typ | Energie | Gold | Effekt |
|---|---|:---:|:---:|---|
| **Scout Tower** | Turmbau | 2 | 2 | Platziert einen Scout Tower |
| **Sniper Tower** | Turmbau | 3 | 3 | Platziert einen Sniper Tower |
| **Rapid Tower** | Turmbau | 2 | 2 | Platziert einen Rapid Tower |
| **Energy Surge** | Utility | 0 | – | +2 Energie sofort |
| **Supply Drop** | Utility | 0 | – | +3 Gold sofort |
| **Overcharge** | Buff | 1 | – | +1 Schaden auf alle platzierten Türme |
| **Long Range** | Buff | 1 | – | +40 Reichweite auf einen ausgewählten Turm |
| **Tactics** | Utility | 0 | – | Zieht 2 zusätzliche Karten |
| **Planning** | Utility | 0 | – | Nächste Karte kostet 1 Energie weniger |
| **Cryo Pulse** | Kontrolle | 1 | – | Alle Gegner 2 Sekunden auf 40% Geschwindigkeit verlangsamt |

> Turm-Karten können **nur** in der Play-Phase gespielt werden. Buff/Utility-Karten sind auch **während der Wave-Phase** spielbar.

---

## Gegner

Drei Gegner-Typen, erzeugt per **Prototype Pattern** aus Vorlagen:

| Gegner | Symbol | Basis-HP | Basis-Speed | Erscheint ab | Spawn-Frequenz |
|---|:---:|---|---|:---:|---|
| **Standard** | 🔴 | 5 + Welle | 54 + Welle×1.6 | Welle 1 | Jeder Spawn |
| **Fast** | 🟡 | 3 + Welle÷2 | 82 + Welle×2.2 | Welle 2 | Jeder 3. Spawn |
| **Tank** | 🟣 | 11 + Welle×2 | 38 + Welle×1.2 | Welle 3 | Jeder 5. Spawn |

**Wellenanstieg:** Welle 1 spawnt 10 Einheiten, jede weitere Welle +2 (max. 28). Das Spawn-Intervall sinkt von 0.75s auf min. 0.32s.

---

## Spielphasen

Das Spiel läuft in einem festen Phasenzyklus (State Pattern):

```
┌──────────┐           ┌──────────┐        [SPACE]        ┌──────────┐
│          │  sofort   │          │ ──────────────────────►│          │
│ DrawPhase│──────────►│ PlayPhase│                        │ WavePhase│
│          │           │          │                        │          │
└──────────┘           └──────────┘                        └────┬─────┘
  • 3 Karten ziehen      • Karten spielen                       │ alle Gegner besiegt
  • Energie reset        • Türme bauen                          ▼
  • → PlayPhase          • Welle starten                  ┌──────────────┐
                                                          │ ResolvePhase │
                                                          │  (0.8 Sek.)  │
                                                          └──────┬───────┘
                                                                 │
                                              ┌──────────────────┴──────────────────┐
                                              │                                     │
                                    Welle < 12                              Welle 12 erreicht
                                     → DrawPhase                         → GameOverPhase (Sieg)

  Basis-HP = 0 → GameOverPhase (Niederlage)  [aus jeder Phase möglich]
```

---

## Design-Pattern-Architektur

Das Projekt implementiert **9 GoF Design Patterns**, die direkt aus den Spielmechaniken abgeleitet sind:

| # | Pattern | Kategorie | Anwendungsfall | Warum dieses Pattern? |
|:---:|---|---|---|---|
| 1 | **Command** | Verhalten | Jede Spielkarte (`Card.execute()`) kapselt eine Aktion als Objekt | `GameManager.playCard()` muss nicht wissen, was eine Karte tut – er ruft nur `execute()` auf. Neue Karten ohne Code-Änderungen am Manager |
| 2 | **Factory Method** | Erzeugung | `CardFactory` / `StrategyCardFactory` erstellt das komplette Kartendeck | Deck-Inhalte sind vollständig austauschbar – eine Test-Factory oder eine andere Karten-Konfiguration erfordert keine Code-Änderungen am `Deck` oder `GameManager` |
| 3 | **Prototype** | Erzeugung | `EnemyPrototype.copy()` klont Gegner-Vorlagen pro Spawn; `Card.copy()` füllt den Stapel | Gegner-Statistiken werden einmal pro Welle konfiguriert, dann pro Spawn geklont. Jede Kopie hat eigene Position, eigene HP – vollständig unabhängig |
| 4 | **Decorator** | Struktur | `DamageBuffDecorator`, `RangeBuffDecorator`, `UpgradeDecorator` stapeln Boni auf Türmen | Beliebig viele Buffs auf jedem Turm kombinierbar, ohne neue Klassen pro Kombination. `getDamage()` delegiert die Aufrufkette durch alle Hüllen |
| 5 | **Strategy** | Verhalten | `TargetingStrategy` (Zielauswahl) und `AttackStrategy` (Angriffsmodus) als austauschbare Algorithmen | Turm-Typen entstehen durch Kombination: Scout = ClosestToGoal + SingleTarget, Rapid = ClosestToGoal + Splash. Neue Varianten ohne bestehenden Code zu ändern |
| 6 | **Observer** | Verhalten | `GameEventBus` für spielweite Events (Logging); Tower-Range-Observer (`onEnemyEnteredRange`) | Logging vollständig entkoppelt vom GameManager. Turm-Angriffssystem nutzt reaktive Range-Events statt direkter Distanzabfragen |
| 7 | **State** | Verhalten | `GamePhase`-Hierarchie: Draw, Play, Wave, Resolve, GameOver | Phasenabhängiges Verhalten (z.B. Turmbau nur in PlayPhase) ohne `if-else`-Ketten. Übergänge explizit in jeder Phase-Klasse definiert |
| 8 | **Singleton** | Erzeugung | `GameManager` – zentraler Koordinator mit globalem Zugriff | Türme, Gegner, Karten und Phase müssen konsistent sein. Zwei Instanzen würden widersprüchliche Zustände produzieren |
| 9 | **Builder** | Erzeugung | `PendingTower` trennt Karten-Spielen (Konfiguration) von Turm-Platzierung (Erstellung) | Zweistufiger Prozess: Karte spielen → Platzierungsmarker; Feld klicken → `build(x,y)` erzeugt den Turm. Validierung (Goldkosten, Gelände) vor dem eigentlichen Bauen |

### Pattern-Interaktionen

Die Stärke liegt nicht in einzelnen Patterns, sondern darin, wie sie ineinandergreifen:

```
Spieler drückt [1] (Overcharge-Karte)
        │
        ▼
GameManager.playCard(0)                  [Singleton: zentraler Einstieg]
        │  energy -= cost
        ▼
DamageBuffCard.execute(context)          [Command: Karte entscheidet selbst]
        │
        ▼
manager.applyTowerModifier(             [Factory: Lambda als TowerDecoratorFactory]
    tower -> new DamageBuffDecorator(tower, 1)
)                                        [Decorator: jeder Turm wird eingehüllt]
        │
        ▼
eventBus.publish(cardPlayed("Overcharge")) [Observer: Logger + weitere Subscriber]
```

```
Welle startet → WavePhase.enter()       [State: Phasenwechsel]
        │
        ▼
WaveSpawner.startWave()
standardPrototype = new EnemyPrototype(STANDARD, speed, hp)
fastPrototype     = new EnemyPrototype(FAST, ...)    [Prototype: Vorlagen konfigurieren]
tankPrototype     = new EnemyPrototype(TANK, ...)
        │
        │  (alle 0.32–0.75s)
        ▼
prototype.copy(board.getPath())          [Prototype: klonen → eigene Pos, HP]
        │
        ▼
tower.getAttackStrategy().attack(tower)  [Strategy: Ziel + Angriffsmodus]
    ├── TargetingStrategy.selectTarget() [Strategy: ClosestToGoal / HighestHP]
    └── target.takeDamage(getDamage())   [Decorator: delegiert durch alle Hüllen]
```

---

## Paketstruktur

```
core/src/main/java/td/core/
│
├── MainGame.java                        # libGDX-Einstiegspunkt, Screen-Verwaltung
│
├── ui/                                  # Darstellung & Eingabe
│   ├── GameScreen.java                  # Hauptspielansicht, Rendering, Input-Handler
│   └── MenuScreen.java                  # Hauptmenü
│
└── model/                               # Spiellogik (keine libGDX-Abhängigkeiten)
    │
    ├── game/                            # Zentrales Spielsystem
    │   ├── GameManager.java             # Singleton – koordiniert alle Subsysteme
    │   ├── WaveSpawner.java             # Gegnerwellen-Steuerung (Prototype)
    │   └── Projectile.java              # Visuelles Projektil-Objekt
    │
    ├── phases/                          # Spielphasenzyklus (State Pattern)
    │   ├── GamePhase.java               # Interface: enter(), update(), getName()
    │   ├── DrawPhase.java               # Karten ziehen, Energie reset → PlayPhase
    │   ├── PlayPhase.java               # Wartet auf Nutzerinteraktion
    │   ├── WavePhase.java               # Gegner spawnen, Welle überwachen
    │   ├── ResolvePhase.java            # 0.8s Delay → DrawPhase
    │   └── GameOverPhase.java           # Endzustand (Sieg oder Niederlage)
    │
    ├── cards/                           # Kartensystem (Command + Factory + Prototype)
    │   ├── Card.java                    # Abstrakte Basisklasse: execute(), copy()
    │   ├── CardContext.java             # Receiver-Zugang für Karten → GameManager
    │   ├── CardFactory.java             # Interface: createDeck()
    │   ├── StrategyCardFactory.java     # Konkrete Factory: 10 Kartentypen konfiguriert
    │   ├── Deck.java                    # Stapel-Verwaltung, klont Prototypen per copy()
    │   ├── Hand.java                    # Handkarten des Spielers
    │   ├── BuildTowerCard.java          # → queueTowerPlacement() (Builder)
    │   ├── DamageBuffCard.java          # → applyDamageBuff() (Decorator)
    │   ├── RangeBuffCard.java           # → queueRangeBuff() (Decorator)
    │   ├── FreezeCard.java              # → slowAllEnemies()
    │   ├── EnergyCard.java              # → addEnergy()
    │   ├── GoldCard.java                # → addGold()
    │   ├── DrawCard.java                # → drawHand()
    │   └── DiscountCard.java            # → addNextCardDiscount()
    │
    ├── towers/                          # Turmsystem (Decorator + Strategy + Builder)
    │   ├── TowerComponent.java          # Interface: getDamage(), getRange(), ...
    │   ├── BaseTower.java               # Kern-Implementierung (Produkt des Builders)
    │   ├── TowerDecorator.java          # Abstrakte Decorator-Basisklasse, delegiert alles
    │   ├── DamageBuffDecorator.java     # überschreibt getDamage() + bonusDamage
    │   ├── RangeBuffDecorator.java      # überschreibt getRange() + bonusRange
    │   ├── UpgradeDecorator.java        # überschreibt Damage, Range, Cooldown, Level
    │   ├── TowerDecoratorFactory.java   # Funktionales Interface: TowerComponent → TowerComponent
    │   ├── PendingTower.java            # Builder: hält Konfiguration, erzeugt via build(x,y)
    │   ├── TowerType.java               # Enum: SCOUT, SNIPER, RAPID
    │   ├── TargetingStrategy.java       # Interface: selectTarget()
    │   ├── AttackStrategy.java          # Interface: attack()
    │   ├── ClosestToGoalStrategy.java   # Ziel: höchster pathIndex
    │   ├── HighestHpStrategy.java       # Ziel: meiste Lebenspunkte
    │   ├── SingleTargetAttackStrategy.java  # Angriff: ein Gegner, voller Schaden
    │   └── SplashAttackStrategy.java    # Angriff: Primärziel + Umkreisschaden
    │
    ├── enemies/                         # Gegnersystem (Prototype)
    │   ├── Enemy.java                   # Bewegungslogik, HP, Slow-Effekt
    │   ├── EnemyPrototype.java          # Konfigurationsvorlage: copy(path) → Enemy
    │   └── EnemyType.java               # Enum: STANDARD, FAST, TANK
    │
    ├── events/                          # Ereignissystem (Observer Pattern)
    │   ├── GameEventBus.java            # Subject: subscribe(), publish()
    │   ├── GameEventListener.java       # Observer-Interface: onEvent()
    │   ├── GameEvent.java               # Wertobjekt mit Factory-Methoden
    │   └── GameEventLogger.java         # Konkreter Observer: Konsolen-Logging
    │
    └── board/                           # Spielfeld
        ├── Board.java                   # 20×20-Raster, Pfad-Liste, buildable-Prüfung
        ├── Tile.java                    # Einzelne Kachel: Koordinaten, path-Flag
        └── BoardComponent.java          # Interface: getX(), getY()
```

**Abhängigkeitsrichtung:** `ui` → `model/game` → alle anderen `model`-Pakete. Die Pakete `phases`, `cards`, `towers`, `enemies`, `events` und `board` haben keine Abhängigkeiten untereinander – sie kennen nur `GameManager` über Methodenparameter.

---

## Steuerung

| Eingabe | Aktion |
|:---:|---|
| `1` – `8` | Karte aus der Hand spielen |
| `SPACE` | Welle starten (Play-Phase → Wave-Phase) |
| `U` | Ausgewählten Turm upgraden (kostet Gold) |
| `Linksklick` | Turm platzieren / Turm auswählen |
| `ESC` | Zurück zum Hauptmenü |
| `R` | Neustart (nach Game Over) |

---

## Technologien

| Technologie | Details |
|---|---|
| **Sprache** | Java 21 |
| **Framework** | libGDX (OrthographicCamera, ShapeRenderer, SpriteBatch, BitmapFont) |
| **Build-Tool** | Gradle 9.3.1 mit Wrapper |
| **Code-Generator** | Lombok (`@Getter`) |
| **Architektur** | Multi-Modul Gradle-Projekt (`core/` + `lwjgl3/`) |
| **Design Patterns** | 9 GoF Patterns (Command, Factory, Prototype, Decorator, Strategy, Observer, State, Singleton, Builder) |

---

## Setup & Installation

**Voraussetzungen:** Java 21+

```bash
# Repository klonen
git clone <repository-url>
cd TD-DesignPattern

# Build
./gradlew build

# Spiel starten (Desktop)
./gradlew lwjgl3:run
```

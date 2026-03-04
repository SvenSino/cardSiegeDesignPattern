⏺ Durch den Em-Dash steht \texttt{TargetingStrategy} nicht mehr am Zeilenanfang und der Absatz bricht anders um.

❯ Die Strategie erhält stets den kumulierten, gebufften Schadenswert, ohne davon
  zu wissen. Die Entkopplung funktioniert vollständig über das gemeinsame Interface
  \texttt{TowerComponent}. ist der satz komisch?

⏺ Ja, „funktioniert vollständig über" ist ungelenk. Besser den Grund direkt nennen:

  „Möglich ist das, weil Strategy und Decorator dasselbe Interface \texttt{TowerComponent} teilen."

⏺ Update(docs/dokumentation/Kap3_Architektur.tex)
  ⎿  Added 2 lines, removed 2 lines
      289  \texttt{Splash\-Attack\-Strategy} \texttt{tower.getDamage()} aufruft, traversiert
      290  dieser Aufruf transparent eine möglicherweise mehrschichtige Decorator-Kette.
      291  Die Strategie erhält stets den kumulierten, gebufften Schadenswert, ohne davon
      292 -zu wissen. Die Entkopplung funktioniert vollständig über das gemeinsame Interface
      293 -\texttt{TowerComponent}.
      292 +zu wissen. Möglich ist das, weil Strategy und Decorator dasselbe Interface
      293 +\texttt{TowerComponent} teilen.
      294
      295  % =============================================================================
      296  \subsection{State - Spielphasenzyklus}

⏺ Erledigt.

❯ was genau heisst das denn?

⏺ Die SplashAttackStrategy ruft tower.getDamage() auf einem TowerComponent auf. Dieses tower könnte in Wirklichkeit ein DamageBuffDecorator(UpgradeDecorator(BaseTower)) sein — aber das sieht die Strategie nicht, weil alle Dekoratoren ebenfalls TowerComponent implementieren. Der Aufruf traversiert die Kette
  automatisch und gibt den summierten Schadenswert zurück.

  Das funktioniert nur, weil Strategy (AttackStrategy) und Decorator (TowerDecorator) beide gegen dasselbe Interface TowerComponent programmiert sind — die Strategie kann gar nicht unterscheiden, ob sie einen nackten Turm oder eine Kette von Wrappern vor sich hat.



❯ Der Einsatz desselben Patterns an zwei unabhängigen Stellen, illustriert                                                                                                                                                                                                                                               
seine Vielseitigkeit. In beiden Fällen liegt der Kern des Problems nicht in der                                                                                                                                                                                                                                    
Erzeugung an sich, sondern in der zentralen Verwaltung von Konfiguration. Es soll eine einzige,                                                                                                                                                                                                                        
verlässliche Quelle der Wahrheit für die Eigenschaften eines Objekttyps geben. kannst du mir den hier nochmal erklären?

⏺ Es geht um zwei konkrete Stellen im Code:

Karten: Die StrategyCardFactory legt einmal fest, wie eine DamageBuffCard aussieht — welchen Bonuswert sie hat, was sie kostet usw. Das ist die eine Quelle der Wahrheit. Das Deck klont davon Kopien, aber die Konfiguration selbst steht nur an einer Stelle. Willst du den Schadenswert ändern, änderst du es in der
Factory — nicht an jedem Ort, wo eine Karte entsteht.

Gegner: Der WaveSpawner legt einmal pro Welle fest, wie ein StandardEnemy dieser Welle aussieht — Geschwindigkeit, HP, skaliert mit der Wellennummer. Das ist wieder die eine Quelle der Wahrheit. Jeder einzelne Spawn klont davon, aber die Werte stehen nur im Prototyp.

Der Satz meint also: Das eigentliche Problem ist nicht "ich brauche mehrere Instanzen" (das wäre trivial mit new), sondern "ich will die Eigenschaften eines Typs an genau einer Stelle definieren und nicht bei jeder Erzeugung wiederholen".

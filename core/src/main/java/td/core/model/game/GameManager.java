package td.core.model.game;

import td.core.model.board.Board;
import td.core.model.cards.Card;
import td.core.model.cards.CardContext;
import td.core.model.cards.CardFactory;
import td.core.model.cards.Deck;
import td.core.model.cards.BuildTowerCard;
import td.core.model.cards.RangeBuffCard;
import td.core.model.phases.DrawPhase;
import td.core.model.enemies.EnemyComponent;
import td.core.model.events.GameEvent;
import td.core.model.events.GameEventBus;
import td.core.model.events.GameEventLogger;
import td.core.model.phases.GameOverPhase;
import td.core.model.phases.GamePhase;
import td.core.model.cards.Hand;
import td.core.model.towers.PendingTower;
import td.core.model.phases.PlayPhase;
import td.core.model.phases.ResolvePhase;
import td.core.model.towers.DamageBuffDecorator;
import td.core.model.towers.RangeBuffDecorator;
import td.core.model.towers.TowerComponent;
import td.core.model.towers.TowerDecoratorFactory;
import td.core.model.towers.UpgradeDecorator;
import td.core.model.phases.WavePhase;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class GameManager {
    private static final GameManager INSTANCE = new GameManager();

    public static GameManager get() {
        return INSTANCE;
    }

    private Board board;
    private int energy;
    private int gold;
    private int baseHealth;
    private int wavesCompleted;
    private int maxWaves;
    private int nextCardDiscount;
    private final List<TowerComponent> towers = new ArrayList<>();
    private final List<EnemyComponent> enemies = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private Deck deck;
    private Hand hand;
    private GamePhase phase;
    private PendingTower pendingTower;
    private TowerDecoratorFactory pendingTowerModifier;

    private final GameEventBus eventBus = new GameEventBus();
    private final WaveSpawner waveSpawner = new WaveSpawner();

    private GameManager() {
        eventBus.subscribe(new GameEventLogger());
    }

    public void init(Board board, CardFactory cardFactory) {
        towers.clear();
        enemies.clear();
        projectiles.clear();
        pendingTower = null;
        pendingTowerModifier = null;
        waveSpawner.reset();

        this.board = board;
        this.deck = new Deck(cardFactory);
        this.hand = new Hand();
        this.energy = 3;
        this.gold = 5;
        this.baseHealth = 10;
        this.wavesCompleted = 0;
        this.maxWaves = 12;
        this.nextCardDiscount = 0;
        this.phase = new DrawPhase();
        this.phase.enter(this);
    }

    public void update(float delta) {
        if (phase instanceof GameOverPhase) {
            return;
        }
        phase.update(this, delta);
        waveSpawner.update(this, delta);
        for (EnemyComponent enemy : enemies) {
            enemy.update(delta);
        }
        updateRangeObservers();
        for (TowerComponent tower : towers) {
            tower.update(delta);
        }

        resolveCombat(delta);
        cleanupEntities();
        updateProjectiles(delta);
    }

    public void playCard(int index) {
        if (!(phase instanceof PlayPhase) && !(phase instanceof WavePhase)) {
            return;
        }
        Card card = hand.removeAt(index);
        if (card == null) {
            return;
        }
        if (!canPlayCard(card)) {
            hand.addToFront(card);
            return;
        }
        int effectiveCost = getEffectiveEnergyCost(card);
        energy -= effectiveCost;
        nextCardDiscount = 0;
        card.execute(new CardContext(this));
        eventBus.publish(GameEvent.cardPlayed(card.getName()));
    }

    public boolean canPlayCard(Card card) {
        if (card == null) {
            return false;
        }
        boolean playOrWave = (phase instanceof PlayPhase) || (phase instanceof WavePhase);
        if (!playOrWave) {
            return false;
        }
        boolean wavePhase = phase instanceof WavePhase;
        boolean buildCard = card instanceof BuildTowerCard;
        boolean targetedBuffCard = card instanceof RangeBuffCard;
        if (wavePhase && buildCard) {
            return false;
        }
        int energyCost = getEffectiveEnergyCost(card);
        if (energy < energyCost) {
            return false;
        }
        if (buildCard && gold < card.getCost()) {
            return false;
        }
        if (targetedBuffCard && towers.isEmpty()) {
            return false;
        }
        return true;
    }

    public int getEffectiveEnergyCost(Card card) {
        return Math.max(0, card.getCost() - nextCardDiscount);
    }

    public void requestWaveStart() {
        if (phase instanceof PlayPhase) {
            switchPhase(new WavePhase());
        }
    }

    public boolean tryPlacePendingTower(int gridX, int gridY) {
        if (pendingTower == null) {
            return false;
        }
        if (!board.isBuildable(gridX, gridY)) {
            return false;
        }
        for (TowerComponent tower : towers) {
            if (tower.getGridX() == gridX && tower.getGridY() == gridY) {
                return false;
            }
        }

        TowerComponent tower = pendingTower.build(gridX, gridY);
        towers.add(tower);
        pendingTower = null;
        gold -= tower.getCost();
        eventBus.publish(GameEvent.towerBuilt(gridX, gridY));
        return true;
    }

    public void queueTowerPlacement(PendingTower pending) {
        if (gold < pending.getCost()) {
            return;
        }
        this.pendingTower = pending;
    }

    public void addEnergy(int amount) {
        energy += amount;
    }

    public void addGold(int amount) {
        gold += amount;
    }

    public void addNextCardDiscount(int amount) {
        nextCardDiscount += amount;
    }

    public void applyTowerModifier(TowerDecoratorFactory factory) {
        List<TowerComponent> updated = new ArrayList<>();
        for (TowerComponent tower : towers) {
            updated.add(factory.apply(tower));
        }
        towers.clear();
        towers.addAll(updated);
    }

    public void applyDamageBuff(int bonusDamage) {
        applyTowerModifier(tower -> new DamageBuffDecorator(tower, bonusDamage));
    }

    public void queueTowerModifier(TowerDecoratorFactory factory) {
        if (factory == null) {
            return;
        }
        pendingTowerModifier = factory;
    }

    public void queueRangeBuff(float bonusRange) {
        queueTowerModifier(tower -> new RangeBuffDecorator(tower, bonusRange));
    }

    public void slowAllEnemies(float duration, float slowFactor) {
        for (EnemyComponent enemy : enemies) {
            enemy.applySlow(duration, slowFactor);
        }
    }

    public TowerComponent tryApplyPendingTowerModifier(TowerComponent tower) {
        if (tower == null || pendingTowerModifier == null) {
            return null;
        }
        int towerIndex = towers.indexOf(tower);
        if (towerIndex < 0) {
            return null;
        }
        TowerComponent modifiedTower = pendingTowerModifier.apply(tower);
        towers.set(towerIndex, modifiedTower);
        pendingTowerModifier = null;
        return modifiedTower;
    }

    public void spawnEnemy(EnemyComponent enemy) {
        enemies.add(enemy);
    }

    public void beginWave() {
        waveSpawner.startWave(this);
        eventBus.publish(GameEvent.WaveStarted);
    }

    public void endWaveIfComplete() {
        if (waveSpawner.isComplete() && enemies.isEmpty()) {
            wavesCompleted++;
            if (wavesCompleted >= maxWaves) {
                switchPhase(new GameOverPhase(true));
            } else {
                switchPhase(new ResolvePhase());
            }
        }
    }

    public void drawHand(int count) {
        boolean firstHandDraw = hand.getCards().isEmpty() && wavesCompleted == 0 && towers.isEmpty();
        for (int i = 0; i < count; i++) {
            hand.add(deck.draw());
        }
        if (firstHandDraw) {
            ensureFirstHandHasTower();
        }
    }

    public void resetEnergy(int base) {
        energy = base;
    }

    public void switchPhase(GamePhase newPhase) {
        phase = newPhase;
        phase.enter(this);
        eventBus.publish(GameEvent.phaseChanged(newPhase.getName()));
    }

    private void resolveCombat(float delta) {
        for (TowerComponent tower : towers) {
            if (tower.canFire()) {
                EnemyComponent target = tower.getAttackStrategy().attack(tower);
                if (target != null) {
                    tower.resetCooldown();
                    spawnProjectile(tower, target);
                }
            } else {
                tower.tickCooldown(delta);
            }
        }
    }

    private void updateRangeObservers() {
        for (TowerComponent tower : towers) {
            Set<EnemyComponent> currentInRange = new HashSet<>();
            for (EnemyComponent component : enemies) {
                if (component.isInRange(tower)) {
                    currentInRange.add(component);
                }
            }

            Set<EnemyComponent> previouslyInRange = new HashSet<>(tower.getEnemiesInRange());
            for (EnemyComponent component : currentInRange) {
                if (!previouslyInRange.contains(component)) {
                    tower.onEnemyEnteredRange(component);
                }
            }
            for (EnemyComponent component : previouslyInRange) {
                if (!currentInRange.contains(component)) {
                    tower.onEnemyExitedRange(component);
                }
            }
        }
    }

    private void cleanupEntities() {
        enemies.removeIf(component -> {
            if (component.isDead()) {
                removeEnemyFromObservers(component);
                addGold(component.getGoldValue());
                eventBus.publish(GameEvent.EnemyDefeated);
                return true;
            }
            if (component.hasReachedGoal()) {
                removeEnemyFromObservers(component);
                baseHealth -= 1;
                eventBus.publish(GameEvent.baseDamaged(baseHealth));
                return true;
            }
            return false;
        });

        if (baseHealth <= 0 && !(phase instanceof GameOverPhase)) {
            switchPhase(new GameOverPhase(false));
        }
    }

    private void removeEnemyFromObservers(EnemyComponent component) {
        for (TowerComponent tower : towers) {
            tower.onEnemyExitedRange(component);
        }
    }

    public TowerComponent upgradeTower(TowerComponent tower) {
        if (tower == null) {
            return null;
        }
        int cost = getUpgradeCost(tower);
        if (gold < cost) {
            return null;
        }
        gold -= cost;
        TowerComponent upgraded = new UpgradeDecorator(tower, 1, 15f, 0.05f);
        int index = towers.indexOf(tower);
        if (index >= 0) {
            towers.set(index, upgraded);
        }
        return upgraded;
    }

    public int getUpgradeCost(TowerComponent tower) {
        return 2 + tower.getLevel();
    }

    private void spawnProjectile(TowerComponent tower, EnemyComponent target) {
        float tileSize = board.getTileSize();
        float fromX = (tower.getGridX() + 0.5f) * tileSize;
        float fromY = (tower.getGridY() + 0.5f) * tileSize;
        Projectile projectile = new Projectile(
            new com.badlogic.gdx.math.Vector2(fromX, fromY),
            target.getPosition(),
            0.15f
        );
        projectiles.add(projectile);
    }

    private void updateProjectiles(float delta) {
        projectiles.removeIf(projectile -> {
            projectile.update(delta);
            return projectile.isExpired();
        });
    }

    private void ensureFirstHandHasTower() {
        if (hand.getCards().stream().anyMatch(card -> card instanceof BuildTowerCard)) {
            return;
        }
        Card replaced = hand.removeAt(0);
        if (replaced == null) {
            return;
        }
        deck.putBottom(replaced);

        for (int i = 0; i < 64; i++) {
            Card candidate = deck.draw();
            if (candidate instanceof BuildTowerCard) {
                hand.addToFront(candidate);
                return;
            }
            deck.putBottom(candidate);
        }

        hand.addToFront(deck.draw());
    }
}

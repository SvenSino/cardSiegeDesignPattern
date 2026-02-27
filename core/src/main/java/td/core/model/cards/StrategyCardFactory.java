package td.core.model.cards;

import td.core.model.towers.ClosestToGoalStrategy;
import td.core.model.towers.HighestHpStrategy;
import td.core.model.towers.SingleTargetAttackStrategy;
import td.core.model.towers.SplashAttackStrategy;
import td.core.model.towers.TowerType;

import java.util.ArrayList;
import java.util.List;

public class StrategyCardFactory implements CardFactory {
    @Override
    public List<Card> createDeck() {
        List<Card> cards = new ArrayList<>();
        cards.add(new BuildTowerCard("Scout Tower", 2, 2, 120f, 0.8f, new ClosestToGoalStrategy(), new SingleTargetAttackStrategy(), TowerType.SCOUT));
        cards.add(new BuildTowerCard("Sniper Tower", 3, 4, 180f, 1.4f, new HighestHpStrategy(), new SingleTargetAttackStrategy(), TowerType.SNIPER));
        cards.add(new BuildTowerCard("Rapid Tower", 2, 1, 100f, 0.4f, new ClosestToGoalStrategy(), new SplashAttackStrategy(36f, 0.6f), TowerType.RAPID));
        cards.add(new EnergyCard("Energy Surge", 0, 2));
        cards.add(new GoldCard("Supply Drop", 0, 3));
        cards.add(new DamageBuffCard("Overcharge", 1, 1));
        cards.add(new RangeBuffCard("Long Range", 1, 40f));
        cards.add(new DrawCard("Tactics", 0, 2));
        cards.add(new DiscountCard("Planning", 0, 1));
        cards.add(new FreezeCard("Cryo Pulse", 1, 2.0f, 0.4f));
        return cards;
    }
}

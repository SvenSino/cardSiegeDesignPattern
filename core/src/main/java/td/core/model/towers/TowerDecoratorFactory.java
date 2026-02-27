package td.core.model.towers;

public interface TowerDecoratorFactory {
    TowerComponent apply(TowerComponent tower);
}
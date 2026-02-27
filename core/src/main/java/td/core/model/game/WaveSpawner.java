package td.core.model.game;

import lombok.Getter;
import td.core.model.enemies.EnemyPrototype;
import td.core.model.enemies.EnemyType;

public class WaveSpawner {
    @Getter
    private boolean active;
    private float timer;
    private int spawned;
    private int totalThisWave;
    private float spawnInterval;
    private int currentWaveNumber;
    private EnemyPrototype standardEnemy;
    private EnemyPrototype fastEnemy;
    private EnemyPrototype tankEnemy;

    public void startWave(GameManager manager) {
        active = true;
        timer = 0f;
        spawned = 0;
        currentWaveNumber = manager.getWavesCompleted() + 1;
        totalThisWave = Math.min(28, 8 + currentWaveNumber * 2);
        spawnInterval = Math.max(0.32f, 0.75f - currentWaveNumber * 0.03f);

        int baseHp = 5 + currentWaveNumber;
        int fastHp = Math.max(3, 3 + currentWaveNumber / 2);
        int tankHp = 11 + currentWaveNumber * 2;

        float baseSpeed = 54f + currentWaveNumber * 1.6f;
        float fastSpeed = 82f + currentWaveNumber * 2.2f;
        float tankSpeed = 38f + currentWaveNumber * 1.2f;

        standardEnemy = new EnemyPrototype(EnemyType.STANDARD, baseSpeed, baseHp);
        fastEnemy = new EnemyPrototype(EnemyType.FAST, fastSpeed, fastHp);
        tankEnemy = new EnemyPrototype(EnemyType.TANK, tankSpeed, tankHp);
    }

    public void reset() {
        active = false;
        timer = 0f;
        spawned = 0;
        totalThisWave = 0;
        spawnInterval = 0f;
        currentWaveNumber = 0;
        standardEnemy = null;
        fastEnemy = null;
        tankEnemy = null;
    }

    public void update(GameManager manager, float delta) {
        if (!active) {
            return;
        }
        timer += delta;
        if (timer >= spawnInterval && spawned < totalThisWave) {
            timer = 0f;
            manager.spawnEnemy(selectPrototypeForIndex(spawned).copy(manager.getBoard().getPath()));
            spawned++;
        }
        if (spawned >= totalThisWave) {
            active = false;
        }
    }

    public boolean isComplete() {
        return !active && spawned >= totalThisWave;
    }

    private EnemyPrototype selectPrototypeForIndex(int index) {
        if (currentWaveNumber >= 3 && index % 5 == 0) {
            return tankEnemy;
        }
        if (currentWaveNumber >= 2 && index % 3 == 0) {
            return fastEnemy;
        }
        return standardEnemy;
    }
}

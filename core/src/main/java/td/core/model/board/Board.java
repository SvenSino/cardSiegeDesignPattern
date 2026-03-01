package td.core.model.board;

import com.badlogic.gdx.math.Vector2;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
public class Board implements BoardComponent {
    private final int width;
    private final int height;
    private final float tileSize;
    private final List<Tile> tiles;
    private final List<Vector2> path;

    private record GridPoint(int x, int y) {}

    private Board(int width, int height, float tileSize, List<Tile> tiles, List<Vector2> path) {
        this.width = width;
        this.height = height;
        this.tileSize = tileSize;
        this.tiles = tiles;
        this.path = path;
    }

    public static Board createDefault(int width, int height, float tileSize) {
        List<Tile> tiles = new ArrayList<>();
        List<Vector2> path = new ArrayList<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                tiles.add(new Tile(x, y, false));
            }
        }

        List<int[]> controlPoints = buildControlPoints(width, height);
        Set<GridPoint> uniqueTiles = new LinkedHashSet<>();
        for (int i = 0; i < controlPoints.size() - 1; i++) {
            int[] a = controlPoints.get(i);
            int[] b = controlPoints.get(i + 1);
            appendOrthogonalSegment(a[0], a[1], b[0], b[1], width, height, uniqueTiles);
        }

        for (GridPoint point : uniqueTiles) {
            int gridX = point.x();
            int gridY = point.y();
            path.add(new Vector2((gridX + 0.5f) * tileSize, (gridY + 0.5f) * tileSize));
            if (gridX >= 0 && gridX < width && gridY >= 0 && gridY < height) {
                int index = gridY * width + gridX;
                Tile old = tiles.get(index);
                tiles.set(index, new Tile(old.getX(), old.getY(), true));
            }
        }

        return new Board(width, height, tileSize, tiles, path);
    }

    private static List<int[]> buildControlPoints(int width, int height) {
        List<int[]> points = new ArrayList<>();
        int mid = clamp(height / 2, 0, height - 1);
        int top = clamp(2, 0, height - 1);
        int top2 = clamp(4, 0, height - 1);
        int bottom = clamp(height - 3, 0, height - 1);
        int bottom2 = clamp(height - 5, 0, height - 1);

        points.add(new int[] {0, mid});
        points.add(new int[] {clamp((int) (width * 0.10f), 0, width - 1), mid});
        points.add(new int[] {clamp((int) (width * 0.10f), 0, width - 1), top});
        points.add(new int[] {clamp((int) (width * 0.26f), 0, width - 1), top});
        points.add(new int[] {clamp((int) (width * 0.26f), 0, width - 1), bottom});
        points.add(new int[] {clamp((int) (width * 0.42f), 0, width - 1), bottom});
        points.add(new int[] {clamp((int) (width * 0.42f), 0, width - 1), top2});
        points.add(new int[] {clamp((int) (width * 0.58f), 0, width - 1), top2});
        points.add(new int[] {clamp((int) (width * 0.58f), 0, width - 1), bottom2});
        points.add(new int[] {clamp((int) (width * 0.74f), 0, width - 1), bottom2});
        points.add(new int[] {clamp((int) (width * 0.74f), 0, width - 1), top});
        points.add(new int[] {width - 1, top});
        return points;
    }

    private static void appendOrthogonalSegment(int x1, int y1, int x2, int y2, int width, int height, Set<GridPoint> out) {
        int cx = x1;
        int cy = y1;
        appendPoint(cx, cy, width, height, out);

        while (cx != x2) {
            cx += Integer.compare(x2, cx);
            appendPoint(cx, cy, width, height, out);
        }
        while (cy != y2) {
            cy += Integer.compare(y2, cy);
            appendPoint(cx, cy, width, height, out);
        }
    }

    private static void appendPoint(int x, int y, int width, int height, Set<GridPoint> out) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return;
        }
        out.add(new GridPoint(x, y));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public boolean isBuildable(int gridX, int gridY) {
        if (gridX < 0 || gridY < 0 || gridX >= width || gridY >= height) {
            return false;
        }
        for (Tile tile : tiles) {
            if (tile.getX() == gridX && tile.getY() == gridY) {
                return !tile.isPath();
            }
        }
        return false;
    }

    public List<Tile> getTiles() {
        return Collections.unmodifiableList(tiles);
    }

    public List<Vector2> getPath() {
        return Collections.unmodifiableList(path);
    }

    @Override
    public int getX() {
        return 0;
    }

    @Override
    public int getY() {
        return 0;
    }
}

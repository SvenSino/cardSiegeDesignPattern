package td.core.model.board;

import lombok.Getter;

@Getter
public class Tile implements BoardComponent {
    private final int x;
    private final int y;
    private final boolean path;

    public Tile(int x, int y, boolean path) {
        this.x = x;
        this.y = y;
        this.path = path;
    }

}
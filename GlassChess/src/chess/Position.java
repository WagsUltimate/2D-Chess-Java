package chess;

/** Immutable board coordinate. x = file (a-h), y = rank from top (0-7), z = board layer. */
public record Position(int x, int y, int z) {
    public Position offset(int dx, int dy) {
        return new Position(x + dx, y + dy, z);
    }

    public String algebraic() {
        return "" + (char) ('a' + x) + (8 - y);
    }
}

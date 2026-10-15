package chess;

/** Each piece is a separate object that owns its current (x, y, z) position. */
public final class Piece {
    private final Side side;
    private PieceType type;
    private Position position;
    private boolean moved;

    public Piece(Side side, PieceType type, Position position) {
        this.side = side;
        this.type = type;
        this.position = position;
    }

    public Side side() { return side; }
    public PieceType type() { return type; }
    public Position position() { return position; }
    public boolean hasMoved() { return moved; }

    void moveTo(Position next) {
        position = next;
        moved = true;
    }

    void promoteTo(PieceType nextType) {
        type = nextType;
    }

    Piece copy() {
        Piece copy = new Piece(side, type, position);
        copy.moved = moved;
        return copy;
    }

    // Used by tests to construct specific situations.
    void setHasMoved(boolean value) { moved = value; }
}

package chess;

/** The physical board owns the squares. Future versions can use layers > 1. */
public final class Board {
    public static final int SIZE = 8;
    private final Piece[][][] squares; // [z][y][x]

    public Board(int layers) {
        if (layers < 1) throw new IllegalArgumentException("At least one layer required");
        squares = new Piece[layers][SIZE][SIZE];
    }

    public int layers() { return squares.length; }

    public boolean contains(Position p) {
        return p != null && p.x() >= 0 && p.x() < SIZE && p.y() >= 0
            && p.y() < SIZE && p.z() >= 0 && p.z() < layers();
    }

    public Piece at(Position p) {
        if (!contains(p)) return null;
        return squares[p.z()][p.y()][p.x()];
    }

    public void place(Piece p) {
        if (!contains(p.position())) throw new IllegalArgumentException("Out of bounds");
        if (at(p.position()) != null) throw new IllegalArgumentException("Square occupied");
        squares[p.position().z()][p.position().y()][p.position().x()] = p;
    }

    /** Board-level movement also supports moving between z layers; rules decide when that is legal. */
    public void movePiece(Position from, Position to) {
        if (!contains(from) || !contains(to)) throw new IllegalArgumentException("Out of bounds");
        if (from.equals(to)) throw new IllegalArgumentException("Source equals destination");
        Piece mover = at(from);
        if (mover == null) throw new IllegalArgumentException("No piece at source");
        Piece target = at(to);
        if (target != null && target.side() == mover.side())
            throw new IllegalArgumentException("Cannot capture your own piece");
        if (target != null && target.type() == PieceType.KING)
            throw new IllegalArgumentException("Kings cannot be captured");
        squares[from.z()][from.y()][from.x()] = null;
        squares[to.z()][to.y()][to.x()] = mover; // captures opposing occupant, if any
        mover.moveTo(to);
    }

    public Piece remove(Position p) {
        if (!contains(p)) throw new IllegalArgumentException("Out of bounds");
        Piece old = at(p);
        squares[p.z()][p.y()][p.x()] = null;
        return old;
    }

    public Board copy() {
        Board clone = new Board(layers());
        for (int z = 0; z < layers(); z++) {
            for (int y = 0; y < SIZE; y++) {
                for (int x = 0; x < SIZE; x++) {
                    Piece piece = squares[z][y][x];
                    if (piece != null) clone.place(piece.copy());
                }
            }
        }
        return clone;
    }

    public static Board normalSetup() {
        Board board = new Board(1);
        PieceType[] backRow = { PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP,
            PieceType.QUEEN, PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK };
        for (int x = 0; x < SIZE; x++) {
            board.place(new Piece(Side.BLACK, backRow[x], new Position(x, 0, 0)));
            board.place(new Piece(Side.BLACK, PieceType.PAWN, new Position(x, 1, 0)));
            board.place(new Piece(Side.WHITE, PieceType.PAWN, new Position(x, 6, 0)));
            board.place(new Piece(Side.WHITE, backRow[x], new Position(x, 7, 0)));
        }
        return board;
    }
}

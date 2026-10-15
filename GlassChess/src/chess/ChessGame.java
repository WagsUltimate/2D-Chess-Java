package chess;

import java.util.ArrayList;
import java.util.List;

/** Ordinary chess rules on layer z=0. Future 3D rules can be added here. */
public final class ChessGame {
    private static final int[][] ROOK_DIRS = {{1,0},{-1,0},{0,1},{0,-1}};
    private static final int[][] BISHOP_DIRS = {{1,1},{1,-1},{-1,1},{-1,-1}};
    private static final int[][] KNIGHT_STEPS = {{1,2},{2,1},{-1,2},{-2,1},{1,-2},{2,-1},{-1,-2},{-2,-1}};
    private static final PieceType[] PROMOTIONS = {
        PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT
    };

    private Board board;
    private Side turn = Side.WHITE;
    private Position enPassantSquare;
    private int moveNumber;

    public ChessGame() { board = Board.normalSetup(); }

    // Package-visible to let our dependency-free tests set up positions.
    ChessGame(Board testBoard, Side toMove, Position passant) {
        board = testBoard;
        turn = toMove;
        enPassantSquare = passant;
    }

    public Board board() { return board; }
    public Side turn() { return turn; }
    public int moveNumber() { return moveNumber; }

    public void restart() {
        board = Board.normalSetup();
        turn = Side.WHITE;
        enPassantSquare = null;
        moveNumber = 0;
    }

    public List<Move> legalMovesFrom(Position from) {
        Piece piece = board.at(from);
        if (piece == null || piece.side() != turn || from.z() != 0) return List.of();
        List<Move> legal = new ArrayList<>();
        for (Move candidate : candidateMoves(board, piece, enPassantSquare)) {
            Board trial = board.copy();
            perform(trial, candidate, enPassantSquare);
            if (!isInCheck(trial, turn)) legal.add(candidate);
        }
        return legal;
    }

    public List<Move> allLegalMoves() {
        List<Move> all = new ArrayList<>();
        for (int y = 0; y < Board.SIZE; y++) {
            for (int x = 0; x < Board.SIZE; x++) {
                all.addAll(legalMovesFrom(new Position(x, y, 0)));
            }
        }
        return all;
    }

    public boolean makeMove(Move move) {
        if (!legalMovesFrom(move.from()).contains(move)) return false;
        Piece mover = board.at(move.from());
        Position newPassant = null;
        if (mover.type() == PieceType.PAWN && Math.abs(move.to().y() - move.from().y()) == 2) {
            newPassant = move.from().offset(0, mover.side() == Side.WHITE ? -1 : 1);
        }
        perform(board, move, enPassantSquare);
        enPassantSquare = newPassant;
        turn = turn.opposite();
        moveNumber++;
        return true;
    }

    public boolean isInCheck(Side side) { return isInCheck(board, side); }

    public boolean isCheckmate() { return isInCheck(turn) && allLegalMoves().isEmpty(); }

    public boolean isStalemate() { return !isInCheck(turn) && allLegalMoves().isEmpty(); }

    public String status() {
        String player = turn == Side.WHITE ? "White" : "Black";
        if (isCheckmate()) return "Checkmate! " + (turn == Side.WHITE ? "Black" : "White") + " wins.";
        if (isStalemate()) return "Stalemate! Draw.";
        return player + " to move" + (isInCheck(turn) ? " — CHECK" : "") + ".";
    }

    private static boolean canTake(Piece piece, Piece target) {
        return target == null || (target.side() != piece.side() && target.type() != PieceType.KING);
    }

    private static void offer(List<Move> moves, Board b, Piece piece, Position dst) {
        if (!b.contains(dst) || !canTake(piece, b.at(dst))) return;
        if (piece.type() == PieceType.PAWN && (dst.y() == 0 || dst.y() == 7)) {
            for (PieceType choice : PROMOTIONS) moves.add(new Move(piece.position(), dst, choice));
        } else {
            moves.add(new Move(piece.position(), dst));
        }
    }

    private static List<Move> candidateMoves(Board b, Piece piece, Position passant) {
        List<Move> moves = new ArrayList<>();
        Position from = piece.position();
        switch (piece.type()) {
            case PAWN -> {
                int dir = piece.side() == Side.WHITE ? -1 : 1;
                int startingRank = piece.side() == Side.WHITE ? 6 : 1;
                Position one = from.offset(0, dir);
                if (b.contains(one) && b.at(one) == null) {
                    offer(moves, b, piece, one);
                    Position two = from.offset(0, 2 * dir);
                    if (from.y() == startingRank && !piece.hasMoved()
                            && b.contains(two) && b.at(two) == null) {
                        offer(moves, b, piece, two);
                    }
                }
                for (int dx : new int[]{-1, 1}) {
                    Position diagonal = from.offset(dx, dir);
                    if (!b.contains(diagonal)) continue;
                    Piece victim = b.at(diagonal);
                    if (victim != null && victim.side() != piece.side()
                            && victim.type() != PieceType.KING) {
                        offer(moves, b, piece, diagonal);
                    } else if (passant != null && diagonal.equals(passant)) {
                        Piece adjacent = b.at(from.offset(dx, 0));
                        if (adjacent != null && adjacent.side() != piece.side()
                                && adjacent.type() == PieceType.PAWN) {
                            offer(moves, b, piece, diagonal);
                        }
                    }
                }
            }
            case KNIGHT -> {
                for (int[] step : KNIGHT_STEPS) offer(moves, b, piece, from.offset(step[0], step[1]));
            }
            case BISHOP -> slide(moves, b, piece, BISHOP_DIRS);
            case ROOK -> slide(moves, b, piece, ROOK_DIRS);
            case QUEEN -> {
                slide(moves, b, piece, BISHOP_DIRS);
                slide(moves, b, piece, ROOK_DIRS);
            }
            case KING -> {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        if (dx != 0 || dy != 0) offer(moves, b, piece, from.offset(dx, dy));
                    }
                }
                // Castling: not moved, path clear, and king does not pass through check.
                int homeRank = piece.side() == Side.WHITE ? 7 : 0;
                if (from.x() == 4 && from.y() == homeRank && !piece.hasMoved()
                        && !isSquareAttacked(b, from, piece.side().opposite())) {
                    castleCandidate(moves, b, piece, 7, 1, 6);
                    castleCandidate(moves, b, piece, 0, -1, 2);
                }
            }
        }
        return moves;
    }

    private static void slide(List<Move> result, Board b, Piece p, int[][] directions) {
        for (int[] d : directions) {
            Position dst = p.position().offset(d[0], d[1]);
            while (b.contains(dst)) {
                Piece target = b.at(dst);
                if (target == null) result.add(new Move(p.position(), dst));
                else {
                    if (canTake(p, target)) result.add(new Move(p.position(), dst));
                    break;
                }
                dst = dst.offset(d[0], d[1]);
            }
        }
    }

    private static void castleCandidate(List<Move> moves, Board b, Piece king,
                                        int rookX, int direction, int finalKingX) {
        Position from = king.position();
        Piece rook = b.at(new Position(rookX, from.y(), from.z()));
        if (rook == null || rook.type() != PieceType.ROOK || rook.side() != king.side()
                || rook.hasMoved()) return;
        for (int x = from.x() + direction; x != rookX; x += direction) {
            if (b.at(new Position(x, from.y(), from.z())) != null) return;
        }
        Position crossing = from.offset(direction, 0);
        Position destination = new Position(finalKingX, from.y(), from.z());
        if (!isSquareAttacked(b, crossing, king.side().opposite())
                && !isSquareAttacked(b, destination, king.side().opposite())) {
            moves.add(new Move(from, destination));
        }
    }

    /** Applies an already-validated chess move to either the real board or a trial copy. */
    private static void perform(Board b, Move move, Position passant) {
        Piece mover = b.at(move.from());
        if (mover.type() == PieceType.PAWN && move.from().x() != move.to().x()
                && b.at(move.to()) == null && move.to().equals(passant)) {
            b.remove(new Position(move.to().x(), move.from().y(), move.from().z()));
        }
        if (mover.type() == PieceType.KING && Math.abs(move.from().x() - move.to().x()) == 2) {
            boolean kingSide = move.to().x() > move.from().x();
            int rookFromX = kingSide ? 7 : 0;
            int rookToX = kingSide ? 5 : 3;
            b.movePiece(new Position(rookFromX, move.from().y(), move.from().z()),
                        new Position(rookToX, move.from().y(), move.from().z()));
        }
        b.movePiece(move.from(), move.to());
        if (move.promotion() != null) b.at(move.to()).promoteTo(move.promotion());
    }

    private static boolean isInCheck(Board b, Side side) {
        for (int z = 0; z < b.layers(); z++) {
            for (int y = 0; y < 8; y++) {
                for (int x = 0; x < 8; x++) {
                    Position pos = new Position(x, y, z);
                    Piece p = b.at(pos);
                    if (p != null && p.side() == side && p.type() == PieceType.KING) {
                        return isSquareAttacked(b, pos, side.opposite());
                    }
                }
            }
        }
        throw new IllegalStateException("Missing " + side + " king on board");
    }

    private static boolean isSquareAttacked(Board b, Position square, Side attacking) {
        // Pawn attacks go diagonally even when the target square is empty.
        int pawnDirection = attacking == Side.WHITE ? -1 : 1;
        for (int dx : new int[]{-1, 1}) {
            Position attackerSquare = square.offset(dx, -pawnDirection);
            Piece p = b.at(attackerSquare);
            if (is(p, attacking, PieceType.PAWN)) return true;
        }
        for (int[] s : KNIGHT_STEPS) {
            if (is(b.at(square.offset(s[0], s[1])), attacking, PieceType.KNIGHT)) return true;
        }
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if ((dx != 0 || dy != 0)
                        && is(b.at(square.offset(dx, dy)), attacking, PieceType.KING)) return true;
            }
        }
        if (attackedBySlider(b, square, attacking, ROOK_DIRS, PieceType.ROOK)) return true;
        return attackedBySlider(b, square, attacking, BISHOP_DIRS, PieceType.BISHOP);
    }

    private static boolean attackedBySlider(Board b, Position square, Side attacking,
                                             int[][] dirs, PieceType expectedType) {
        for (int[] d : dirs) {
            Position p = square.offset(d[0], d[1]);
            while (b.contains(p)) {
                Piece target = b.at(p);
                if (target != null) {
                    if (target.side() == attacking
                            && (target.type() == PieceType.QUEEN || target.type() == expectedType)) {
                        return true;
                    }
                    break;
                }
                p = p.offset(d[0], d[1]);
            }
        }
        return false;
    }

    private static boolean is(Piece p, Side side, PieceType type) {
        return p != null && p.side() == side && p.type() == type;
    }
}

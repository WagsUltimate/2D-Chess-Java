package chess;

public enum PieceType {
    KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN;

    public String assetName() {
        return name().toLowerCase();
    }
}

package chess;

/** Promotion is null unless the player is choosing a pawn promotion. */
public record Move(Position from, Position to, PieceType promotion) {
    public Move(Position from, Position to) {
        this(from, to, null);
    }

    @Override
    public String toString() {
        return from.algebraic() + to.algebraic()
            + (promotion == null ? "" : "=" + promotion.name().charAt(0));
    }
}

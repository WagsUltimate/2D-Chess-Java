package chess;

import java.util.List;

/** Run with: java -ea -cp out chess.ChessTests (after compiling tests). */
public final class ChessTests {
    private static Position p(String name) {
        return new Position(name.charAt(0)-'a', 8-(name.charAt(1)-'0'), 0);
    }
    private static void ok(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    private static void play(ChessGame g, String from, String to) {
        ok(g.makeMove(new Move(p(from), p(to))), from + to + " should be legal");
    }
    private static boolean has(ChessGame g, String from, String to) {
        return g.legalMovesFrom(p(from)).stream().anyMatch(m -> m.to().equals(p(to)));
    }
    public static void main(String[] args) {
        ChessGame g = new ChessGame();
        ok(g.board().layers() == 1, "default is one layer");
        ok(g.allLegalMoves().size() == 20, "opening has 20 legal moves");
        ok(g.board().at(p("e2")).position().z() == 0, "piece owns z coordinate");
        ok(!has(g, "e2", "e5"), "pawn may not jump 3 squares");
        play(g, "e2", "e4");
        ok(g.allLegalMoves().size() == 20, "black has 20 replies");
        play(g, "e7", "e5");
        play(g, "g1", "f3");
        play(g, "b8", "c6");
        ok(!g.isInCheck(Side.WHITE), "normal opening no check");

        // Fool's mate ends in checkmate.
        g.restart();
        play(g, "f2", "f3");
        play(g, "e7", "e5");
        play(g, "g2", "g4");
        play(g, "d8", "h4");
        ok(g.isCheckmate(), "Fool's mate checkmate");
        ok(g.allLegalMoves().isEmpty(), "no moves after mate");

        // Pawn promotion offers all four options.
        Board promotion = new Board(1);
        promotion.place(new Piece(Side.WHITE, PieceType.KING, p("h1")));
        promotion.place(new Piece(Side.BLACK, PieceType.KING, p("h8")));
        promotion.place(new Piece(Side.WHITE, PieceType.PAWN, p("a7")));
        ChessGame promotionGame = new ChessGame(promotion, Side.WHITE, null);
        long promotions = promotionGame.legalMovesFrom(p("a7")).stream()
                .filter(m -> m.to().equals(p("a8"))).count();
        ok(promotions == 4, "four promotion choices");
        ok(promotionGame.makeMove(new Move(p("a7"), p("a8"), PieceType.KNIGHT)), "underpromotion");
        ok(promotionGame.board().at(p("a8")).type() == PieceType.KNIGHT, "promoted knight");

        // En passant after an immediately preceding double push.
        Board ep = new Board(1);
        ep.place(new Piece(Side.WHITE, PieceType.KING, p("e1")));
        ep.place(new Piece(Side.BLACK, PieceType.KING, p("e8")));
        ep.place(new Piece(Side.WHITE, PieceType.PAWN, p("e5")));
        ep.place(new Piece(Side.BLACK, PieceType.PAWN, p("d7")));
        ChessGame enPassant = new ChessGame(ep, Side.BLACK, null);
        play(enPassant, "d7", "d5");
        ok(has(enPassant, "e5", "d6"), "en passant available");
        play(enPassant, "e5", "d6");
        ok(enPassant.board().at(p("d5")) == null, "en passant captured pawn removed");

        // Castling both ways on a clear, safe board.
        Board castles = new Board(1);
        castles.place(new Piece(Side.WHITE, PieceType.KING, p("e1")));
        castles.place(new Piece(Side.WHITE, PieceType.ROOK, p("a1")));
        castles.place(new Piece(Side.WHITE, PieceType.ROOK, p("h1")));
        castles.place(new Piece(Side.BLACK, PieceType.KING, p("e8")));
        ChessGame castleGame = new ChessGame(castles, Side.WHITE, null);
        ok(has(castleGame, "e1", "g1") && has(castleGame, "e1", "c1"), "castle both ways");
        play(castleGame, "e1", "g1");
        ok(castleGame.board().at(p("f1")).type() == PieceType.ROOK, "rook moves on castling");

        // A pinned piece cannot move sideways to expose its king to check.
        Board pinned = new Board(1);
        pinned.place(new Piece(Side.WHITE, PieceType.KING, p("e1")));
        pinned.place(new Piece(Side.WHITE, PieceType.ROOK, p("e2")));
        pinned.place(new Piece(Side.BLACK, PieceType.ROOK, p("e8")));
        pinned.place(new Piece(Side.BLACK, PieceType.KING, p("a8")));
        ChessGame pinnedGame = new ChessGame(pinned, Side.WHITE, null);
        ok(!has(pinnedGame, "e2", "d2"), "can't expose own king to rook");

        // Castling cannot pass through a square an opponent attacks.
        Board threatened = new Board(1);
        threatened.place(new Piece(Side.WHITE, PieceType.KING, p("e1")));
        threatened.place(new Piece(Side.WHITE, PieceType.ROOK, p("h1")));
        threatened.place(new Piece(Side.BLACK, PieceType.ROOK, p("f8")));
        threatened.place(new Piece(Side.BLACK, PieceType.KING, p("a8")));
        ChessGame threatenedGame = new ChessGame(threatened, Side.WHITE, null);
        ok(!has(threatenedGame, "e1", "g1"), "cannot castle across attacked f1");

        // Stalemate is not checkmate and has no legal moves.
        Board locked = new Board(1);
        locked.place(new Piece(Side.BLACK, PieceType.KING, p("h8")));
        locked.place(new Piece(Side.WHITE, PieceType.KING, p("f7")));
        locked.place(new Piece(Side.WHITE, PieceType.QUEEN, p("g6")));
        ChessGame lockedGame = new ChessGame(locked, Side.BLACK, null);
        ok(lockedGame.isStalemate() && !lockedGame.isCheckmate(), "stalemate recognized");

        // En passant is forbidden if removing the pawns exposes our king.
        Board epPin = new Board(1);
        epPin.place(new Piece(Side.WHITE, PieceType.KING, p("e5")));
        epPin.place(new Piece(Side.WHITE, PieceType.PAWN, p("f5")));
        epPin.place(new Piece(Side.BLACK, PieceType.PAWN, p("g5")));
        epPin.place(new Piece(Side.BLACK, PieceType.ROOK, p("h5")));
        epPin.place(new Piece(Side.BLACK, PieceType.KING, p("a8")));
        ChessGame epPinGame = new ChessGame(epPin, Side.WHITE, p("g6"));
        ok(!has(epPinGame, "f5", "g6"), "pinned pawn cannot capture en passant");

        // Board itself can move a piece along z even though vanilla game rules can't.
        Board layers = new Board(3);
        Piece testPiece = new Piece(Side.WHITE, PieceType.ROOK, new Position(0, 0, 0));
        layers.place(testPiece);
        layers.movePiece(new Position(0, 0, 0), new Position(0, 0, 2));
        ok(testPiece.position().z() == 2, "underlying Board permits future z movement");
        ok(layers.at(new Position(0,0,0)) == null, "old layer vacated");
        System.out.println("All chess rule and z-coordinate tests passed.");
    }
}

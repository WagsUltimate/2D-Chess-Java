package chess;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.List;

/** 2D top-down view. It paints one layer; it does NOT decide which moves are legal. */
public final class ChessPanel extends JPanel {
    private static final double GRID_OFFSET_X = 0.102;
    private static final double GRID_OFFSET_Y = 0.098;
    private static final double GRID_SIZE = 0.796;
    private final ChessGame game;
    private final JLabel status;
    private final BufferedImage boardArt;
    private final EnumMap<Side, EnumMap<PieceType, BufferedImage>> sprites =
            new EnumMap<>(Side.class);
    private Position selected;
    private Position lastFrom;
    private Position lastTo;
    private int visibleLayer = 0;

    public ChessPanel(ChessGame game, JLabel status) {
        this.game = game;
        this.status = status;
        setPreferredSize(new Dimension(790, 790));
        setBackground(new Color(19, 32, 46));
        boardArt = load("assets/board.png");
        for (Side side : Side.values()) {
            EnumMap<PieceType, BufferedImage> sideImages = new EnumMap<>(PieceType.class);
            for (PieceType type : PieceType.values()) {
                sideImages.put(type, load("assets/" + side.name().toLowerCase()
                        + "_" + type.assetName() + ".png"));
            }
            sprites.put(side, sideImages);
        }
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { handleClick(e.getX(), e.getY()); }
        });
        refreshStatus();
    }

    public void reset() {
        game.restart();
        selected = null;
        lastFrom = lastTo = null;
        visibleLayer = 0;
        refreshStatus();
        repaint();
    }

    /** Ready for board switching UI when 3D rules are introduced. */
    public void showLayer(int z) {
        if (z < 0 || z >= game.board().layers()) throw new IllegalArgumentException("Bad layer");
        visibleLayer = z;
        selected = null;
        refreshStatus();
        repaint();
    }

    private void refreshStatus() {
        status.setText("Layer " + visibleLayer + "   |   " + game.status());
    }

    private void handleClick(int mx, int my) {
        Rectangle square = boardBounds();
        double x0 = square.x + square.width * GRID_OFFSET_X;
        double y0 = square.y + square.height * GRID_OFFSET_Y;
        double step = square.width * GRID_SIZE / 8.0;
        int x = (int) Math.floor((mx - x0) / step);
        int y = (int) Math.floor((my - y0) / step);
        if (x < 0 || x > 7 || y < 0 || y > 7 || game.allLegalMoves().isEmpty()) return;
        Position click = new Position(x, y, visibleLayer);
        Piece atClick = game.board().at(click);

        if (selected != null) {
            List<Move> possible = game.legalMovesFrom(selected).stream()
                .filter(m -> m.to().equals(click)).toList();
            if (!possible.isEmpty()) {
                Move chosen = possible.get(0);
                if (possible.size() > 1) {
                    PieceType promotion = askPromotion();
                    if (promotion == null) return;
                    for (Move m : possible) {
                        if (m.promotion() == promotion) chosen = m;
                    }
                }
                if (game.makeMove(chosen)) {
                    lastFrom = chosen.from();
                    lastTo = chosen.to();
                    selected = null;
                    refreshStatus();
                    repaint();
                    return;
                }
            }
        }
        selected = atClick != null && atClick.side() == game.turn() ? click : null;
        repaint();
    }

    private PieceType askPromotion() {
        String[] options = {"Queen", "Rook", "Bishop", "Knight"};
        int choice = JOptionPane.showOptionDialog(this, "Promote pawn to:", "Pawn promotion",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
        if (choice < 0) return null;
        return switch (choice) {
            case 0 -> PieceType.QUEEN;
            case 1 -> PieceType.ROOK;
            case 2 -> PieceType.BISHOP;
            default -> PieceType.KNIGHT;
        };
    }

    private Rectangle boardBounds() {
        int size = Math.min(getWidth(), getHeight()) - 8;
        return new Rectangle((getWidth() - size) / 2, (getHeight() - size) / 2, size, size);
    }

    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        Rectangle bounds = boardBounds();
        if (boardArt != null) {
            g.drawImage(boardArt, bounds.x, bounds.y, bounds.width, bounds.height, null);
        } else {
            drawFallbackBoard(g, bounds);
        }
        double x0 = bounds.x + bounds.width * GRID_OFFSET_X;
        double y0 = bounds.y + bounds.height * GRID_OFFSET_Y;
        double step = bounds.width * GRID_SIZE / 8.0;
        // Keep highlights below the pieces.
        for (Position previous : new Position[]{lastFrom, lastTo}) {
            if (previous != null && previous.z() == visibleLayer) {
                drawHighlight(g, previous, x0, y0, step, new Color(248, 211, 97, 82));
            }
        }
        if (selected != null && selected.z() == visibleLayer) {
            drawHighlight(g, selected, x0, y0, step, new Color(80, 203, 255, 100));
            g.setColor(new Color(117, 242, 193, 210));
            for (Move move : game.legalMovesFrom(selected)) {
                Position p = move.to();
                double cx = x0 + (p.x() + 0.5) * step;
                double cy = y0 + (p.y() + 0.5) * step;
                if (game.board().at(p) == null) {
                    int size = Math.max(8, (int) (step * .20));
                    g.fillOval((int) cx - size / 2, (int) cy - size / 2, size, size);
                } else {
                    g.setStroke(new BasicStroke(Math.max(2f, (float) (step * .045))));
                    int radius = (int) (step * .43);
                    g.drawOval((int) cx - radius, (int) cy - radius, 2 * radius, 2 * radius);
                }
            }
        }
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                Piece p = game.board().at(new Position(x, y, visibleLayer));
                if (p == null) continue;
                BufferedImage sprite = sprites.get(p.side()).get(p.type());
                int size = (int) (step * .94);
                int px = (int) Math.round(x0 + x * step + (step - size) / 2);
                int py = (int) Math.round(y0 + y * step + (step - size) / 2);
                if (sprite != null) {
                    g.drawImage(sprite, px, py, size, size, null);
                } else {
                    drawFallbackPiece(g, p, px, py, size);
                }
            }
        }
        g.dispose();
    }

    private void drawHighlight(Graphics2D g, Position p, double x0, double y0,
                               double step, Color color) {
        g.setColor(color);
        g.fillRect((int) Math.round(x0 + p.x() * step),
                (int) Math.round(y0 + p.y() * step),
                (int) Math.ceil(step), (int) Math.ceil(step));
    }

    private void drawFallbackBoard(Graphics2D g, Rectangle b) {
        double x0 = b.x + b.width * GRID_OFFSET_X;
        double y0 = b.y + b.height * GRID_OFFSET_Y;
        double tile = b.width * GRID_SIZE / 8.0;
        g.setColor(new Color(77, 133, 166));
        g.fillRoundRect(b.x, b.y, b.width, b.height, 16, 16);
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                g.setColor((x + y) % 2 == 0 ? new Color(207, 232, 244) : new Color(27, 47, 70));
                g.fillRect((int) (x0 + x * tile), (int) (y0 + y * tile),
                        (int) Math.ceil(tile), (int) Math.ceil(tile));
            }
        }
    }

    private void drawFallbackPiece(Graphics2D g, Piece p, int x, int y, int size) {
        String symbols = p.side() == Side.WHITE ? "♔♕♖♗♘♙" : "♚♛♜♝♞♟";
        PieceType[] order = {PieceType.KING, PieceType.QUEEN, PieceType.ROOK,
                PieceType.BISHOP, PieceType.KNIGHT, PieceType.PAWN};
        int idx = 0;
        while (order[idx] != p.type()) idx++;
        g.setFont(new Font("Serif", Font.PLAIN, (int) (size * 0.90)));
        g.setColor(p.side() == Side.WHITE ? new Color(235, 244, 255) : new Color(13, 21, 34));
        String symbol = String.valueOf(symbols.charAt(idx));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(symbol, x + (size - fm.stringWidth(symbol)) / 2,
                y + (size - fm.getHeight()) / 2 + fm.getAscent());
    }

    private static BufferedImage load(String path) {
        try {
            File f = new File(path);
            return f.exists() ? ImageIO.read(f) : null;
        } catch (IOException e) {
            System.err.println("Could not load " + path + ": " + e.getMessage());
            return null;
        }
    }
}

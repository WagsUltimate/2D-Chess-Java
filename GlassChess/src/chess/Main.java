package chess;

import javax.swing.*;
import java.awt.*;

public final class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::start);
    }

    private static void start() {
        JFrame frame = new JFrame("Glass Chess — Vanilla Edition");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        ChessGame game = new ChessGame();
        JLabel status = new JLabel();
        status.setForeground(new Color(225, 241, 252));
        status.setFont(new Font("SansSerif", Font.BOLD, 16));
        ChessPanel panel = new ChessPanel(game, status);

        JButton newGame = new JButton("New game");
        newGame.addActionListener(e -> panel.reset());
        JPanel controls = new JPanel(new BorderLayout(16, 0));
        controls.setBackground(new Color(19, 32, 46));
        controls.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
        controls.add(status, BorderLayout.CENTER);
        controls.add(newGame, BorderLayout.EAST);

        frame.add(panel, BorderLayout.CENTER);
        frame.add(controls, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setMinimumSize(new Dimension(480, 530));
        frame.setVisible(true);
    }
}

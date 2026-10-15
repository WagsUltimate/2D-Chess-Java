# Glass Chess — Java starter (standard chess, z-ready)

A dependency-free Java 17+ Swing chess game. The appearance uses glass/crystal artwork, but gameplay is strictly **regular two-player chess** on **layer `z = 0`**.

The source is divided into small classes so a second developer can extend the board and game rules into a multi-layer variant without rebuilding the entire application. It uses a top-down **2D display**; no 3D renderer is required.

## Run on Windows

1. Install a JDK (Java 17 or newer).
2. Extract this repository, including the `assets` folder.
3. Double-click `run.bat`, or execute `run.bat` in Command Prompt **from the project folder**.

If Windows cannot find `javac`, configure the JDK in your `PATH` or launch from Eclipse.

## Run in Eclipse

1. **File → Import → General → Existing Projects into Workspace**.
2. Choose the `GlassChess` folder as the root directory and import the project.
3. Open `src/chess/Main.java` and choose **Run As → Java Application**.
4. In Run Configurations, make sure **Working directory** is the `GlassChess` project folder, so artwork is loaded from `assets/`.

## Run in a terminal (macOS / Linux)

```bash
./run.sh
```

Alternatively, without scripts:

```bash
javac -encoding UTF-8 -d out src/chess/*.java
java -cp out chess.Main
```

**Windows manual commands:**

```bat
javac -encoding UTF-8 -d out src\chess\*.java
java -cp out chess.Main
```

## Controls

- Click one of the current side's pieces to select it.
- Legal destinations appear as green markers.
- Click a marker to move or capture.
- Pawn promotions prompt for Queen, Rook, Bishop, or Knight.
- Press **New game** to start over.

Supported rules include standard piece movement, legal-move checking, castling, en passant, pawn promotion, check, checkmate, and stalemate. **Not yet implemented:** claiming draws by repetition or the 50-move rule, insufficient-material draw detection, timers, AI opponents, undo, move notation/history, or any 3D variant movement. These are deliberate later milestones.

## Files / object responsibilities

| File | Purpose |
| --- | --- |
| `src/chess/Position.java` | Immutable `(x, y, z)` coordinate. |
| `src/chess/Piece.java` | Piece **object**: owner, type, coordinate, moved state. |
| `src/chess/Board.java` | Board **object**: 8×8×layers storage, gets/places/moves pieces; `normalSetup()` places normal chess pieces. |
| `src/chess/Side.java` | White/black side enumeration. |
| `src/chess/PieceType.java` | Six piece types. |
| `src/chess/Move.java` | Move object with optional pawn promotion. |
| `src/chess/ChessGame.java` | Standard chess legal moves, check and game-end rules. |
| `src/chess/ChessPanel.java` | Visual Swing board, mouse handling, glass sprites. |
| `src/chess/Main.java` | Application entry point and window. |
| `tests/chess/ChessTests.java` | Dependency-free rule and layer tests. |
| `assets/*.png` | Glass chessboard and individual piece sprites. |

## Coordinate convention

- `x = 0..7`: `a..h` (left to right)
- `y = 0..7`: rank 8 down to rank 1 (top to bottom)
- `z = 0`: the **only** standard chess board for the current version

For example: `new Position(4, 6, 0)` is **e2**. `new Position(4, 6, 1)` would be the same square on a future second board, but no game rule allows it yet.

`Board` already uses a 3D array `Piece[z][y][x]`, can be constructed with multiple layers, and supports physically moving a piece to another `z`. `ChessGame` currently returns legal moves only from the standard board at `z = 0`. To implement the future variant, change **the rules** separately; do not call `Board.movePiece` directly from the UI to bypass move validation.

`ChessPanel` already has `showLayer(int z)` for when another layer exists, but this first version does not show a layer-switching button because there is only one playable board.

## Run tests

Windows:

```bat
test.bat
```

macOS/Linux:

```bash
./test.sh
```

The tests cover opening moves, checkmate, en passant, promotion, castling, and physical z-coordinate movement.

## Notes for GitHub

This is a source project; you can create a GitHub repository and commit the `src`, `tests`, and `assets` folders along with `README.md`, scripts, and the Eclipse project metadata. Do not commit the compiled `out` or `bin` directories (`.gitignore` excludes them).

### Visual artwork

The glass board and pieces were generated for this project. PNG assets are part of the repository, with cropped transparent-background sprites for each piece. The white king asset is an adjusted-color version of the dark king sprite so the shapes match; we can replace it later with the original clear-glass render.

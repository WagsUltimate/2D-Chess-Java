# 2D Chess (Haskell)

A complete command-line 2D Chess implementation in Haskell, structured to mirror the `2D-Chess-Java-main` layout.

## Project Structure

```text
2D-Chess-Haskell-main/
├── .location
├── .markers
├── 2D-Chess.cabal
├── README.md
├── org.ghc.haskell.apt.core/
└── src/
    └── Main.hs
```

## Features

- Full 8x8 board representation with Unicode chess piece rendering (`♔♕♖♗♘♙` / `♚♛♜♝♞♟`)
- Complete legal move generation & path collision checks for all piece types
- Check, Checkmate, and Stalemate detection
- Automatic Pawn-to-Queen promotion
- Algebraic coordinate input (e.g., `e2 e4` or `e2e4`)

## Build & Run

### Using Cabal
```bash
cabal run
```

### Using GHC Directly
```bash
ghc -O2 src/Main.hs -o chess-hs
./chess-hs
```

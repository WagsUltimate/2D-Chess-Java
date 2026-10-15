#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
mkdir -p out
javac -encoding UTF-8 -d out src/chess/*.java tests/chess/ChessTests.java
java -ea -cp out chess.ChessTests

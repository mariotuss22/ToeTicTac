package com.byers.toetictac;

import java.util.ArrayList;
import java.util.Collections;

public class GameEngine {

    GameState state;
    boolean infiniteMode, gambleMode, shuffleMode, singlePlayer, ultimateTTT, ultimateMode;
    boolean aiIsX = false;
    public GameEngine(GameState state) {
        this.state = state;
    }

    public void setModes(boolean inf, boolean gamble, boolean shuffle, boolean single, boolean ultimate) {
        infiniteMode = inf;
        gambleMode = gamble;
        shuffleMode = shuffle;
        singlePlayer = single;
        ultimateTTT = ultimate;
    }

    public Object playMove(int r, int c) {
        if (state.board[r][c] != null) {
            return null;
        }
        String player = state.xTurn ? "X" : "O";

        //GAMBLE
        if (gambleMode) {
            int roll = (int)(Math.random() * 100) + 1;
            if (roll <= 40) {
                player = "X";
            } else if (roll <= 80) {
                player = "O";
            } else if (roll <= 90) {
                player = state.xTurn ? "O" : "X";
            } else {
                player = Math.random() < 0.5 ? "X" : "O";
            }
        }

        state.lastMovePlayer = player;
        state.board[r][c] = player;
        state.moveHistory.add(new int[]{r, c});
        state.turnCount++;

        //INFINITE
        if (infiniteMode) {
            applyDecay();
        }

        //SHUFFLE
        if (shuffleMode) {
            shuffle();
        }

        // CHECK WIN
        int[][] win = checkWin(player);
        if (win != null) {
            return win;
        }

        if (isDraw()) {
            return "DRAW";
        }

        // flip turn if no reset happened
        state.xTurn = !state.xTurn;

        return null;
    }
    public boolean isDraw() {
        for (int i = 0; i < state.boardSize; i++) {
            for (int j = 0; j < state.boardSize; j++) {
                if (state.board[i][j] == null) {
                    return false;
                }
            }
        }
        return true;
    }

    //Shuffle mode
    public void shuffle() {
        ArrayList<String> pieces = new ArrayList<>();
        ArrayList<int[]> positions = new ArrayList<>();

        // collect all pieces & their positions
        for (int i = 0; i < state.boardSize; i++) {
            for (int j = 0; j < state.boardSize; j++) {
                if (state.board[i][j] != null) {
                    pieces.add(state.board[i][j]);
                    positions.add(new int[]{i, j});
                }
            }
        }

        // shuffle pieces
        Collections.shuffle(pieces);

        // reassign shuffled pieces back into same slots
        for (int k = 0; k < positions.size(); k++) {
            int r = positions.get(k)[0];
            int c = positions.get(k)[1];
            state.board[r][c] = pieces.get(k);
        }
    }

    //Inf mode
    public void applyDecay() {
        int limit = state.boardSize +2;
        int[] oldestMove = state.moveHistory.removeFirst();
        if (!infiniteMode) {
            return;
        }
        if (state.moveHistory.size() <= limit) {
            return;
        }
        state.board[oldestMove[0]][oldestMove[1]] = null;
    }

    //Ai single mode
    public int[] getAIMove() {

        int n = state.boardSize;
        String[][] b = state.board;

        // 1. Try to WIN
        int[] winMove = findWinningMove("O");
        if (winMove != null) return winMove;

        // 2. Block PLAYER
        int[] blockMove = findWinningMove("X");
        if (blockMove != null) return blockMove;

        // 3. Take center if available
        int center = n / 2;
        if (b[center][center] == null) {
            return new int[]{center, center};
        }

        // 4. Take a corner
        int[][] corners = {
                {0,0},
                {0,n-1},
                {n-1,0},
                {n-1,n-1}
        };
        for (int[] c : corners) {
            if (b[c[0]][c[1]] == null) {
                return c;
            }
        }
        // 5. fallback random
        ArrayList<int[]> moves = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (b[i][j] == null) {
                    moves.add(new int[]{i, j});
                }
            }
        }
        if (moves.isEmpty()) return null;
        return moves.get((int)(Math.random() * moves.size()));
    }
    private int[] findWinningMove(String player) {
        int n = state.boardSize;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (state.board[i][j] != null) continue;
                state.board[i][j] = player;
                boolean wins = checkWin(player) != null;
                state.board[i][j] = null;
                if (wins) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }

    //UltimateTTT
    public void setUltimateMode(boolean ultimate) {
        this.ultimateMode = ultimate;
    }
    public Object playUltimateMove(int br, int bc) {
        String[][] mini = state.ultimateBoards[br][bc];
        String player = state.xTurn ? "X" : "O";
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (mini[i][j] == null) {
                    mini[i][j] = player;
                    if (checkMiniWin(mini, player)) {
                        state.ultimateMetaBoard[br][bc] = player;
                    }
                    state.xTurn = !state.xTurn;
                    return mini;
                }
            }
        }
        return mini;
    }
    private boolean checkMiniWin(String[][] b, String p) {

        for (int i = 0; i < 3; i++) {
            if (p.equals(b[i][0]) && p.equals(b[i][1]) && p.equals(b[i][2])) return true;
            if (p.equals(b[0][i]) && p.equals(b[1][i]) && p.equals(b[2][i])) return true;
        }

        if (p.equals(b[0][0]) && p.equals(b[1][1]) && p.equals(b[2][2])) return true;
        if (p.equals(b[0][2]) && p.equals(b[1][1]) && p.equals(b[2][0])) return true;

        return false;
    }
    public boolean checkUltimateWin(String p) {
        String[][] b = state.ultimateMetaBoard;
        for (int i = 0; i < 3; i++) {
            if (p.equals(b[i][0]) && p.equals(b[i][1]) && p.equals(b[i][2])) return true;
            if (p.equals(b[0][i]) && p.equals(b[1][i]) && p.equals(b[2][i])) return true;
        }
        if (p.equals(b[0][0]) && p.equals(b[1][1]) && p.equals(b[2][2])) return true;
        if (p.equals(b[0][2]) && p.equals(b[1][1]) && p.equals(b[2][0])) return true;
        return false;
    }


    public int[][] checkWin(String player) {
        int n = state.boardSize;
        String[][] b = state.board;
        // ROWS
        for (int i = 0; i < n; i++) {
            boolean win = true;
            for (int j = 0; j < n; j++) {
                if (!player.equals(b[i][j])) {
                    win = false;
                    break;
                }
            }
            if (win) {
                int[][] cells = new int[n][2];
                for (int j = 0; j < n; j++) {
                    cells[j][0] = i;
                    cells[j][1] = j;
                }
                return cells;
            }
        }
        // COLS
        for (int j = 0; j < n; j++) {
            boolean win = true;
            for (int i = 0; i < n; i++) {
                if (!player.equals(b[i][j])) {
                    win = false;
                    break;
                }
            }
            if (win) {
                int[][] cells = new int[n][2];
                for (int i = 0; i < n; i++) {
                    cells[i][0] = i;
                    cells[i][1] = j;
                }
                return cells;
            }
        }
        // DIAGONAL 1
        boolean win = true;
        for (int i = 0; i < n; i++) {
            if (!player.equals(b[i][i])) {
                win = false;
            }
        }
        if (win) {
            int[][] cells = new int[n][2];
            for (int i = 0; i < n; i++) {
                cells[i][0] = i;
                cells[i][1] = i;
            }
            return cells;
        }
        // DIAGONAL 2
        win = true;
        for (int i = 0; i < n; i++) {
            if (!player.equals(b[i][n - 1 - i])) {
                win = false;
            }
        }
        if (win) {
            int[][] cells = new int[n][2];
            for (int i = 0; i < n; i++) {
                cells[i][0] = i;
                cells[i][1] = n - 1 - i;
            }
            return cells;
        }
        return null;
    }
    public void reset() {
        for (int i = 0; i < state.boardSize; i++) {
            for (int j = 0; j < state.boardSize; j++) {
                state.board[i][j] = null;
            }
        }

        state.turnCount = 0;
        state.xTurn = true;
    }

}
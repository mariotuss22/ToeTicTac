package com.byers.toetictac;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Menu extends AppCompatActivity {

    GameState state;
    GameEngine engine;
    GridLayout board;
    TextView turnAlert, p1Score, p2Score;;
    int boardSize, bgColor, textColor, aiDotCount;
    float currentRed = 20, currentGreen = 20, currentBlue = 20;
    boolean infinite, gamble, shuffle, single, ultimate, aiThinking, gameOver;

    private android.os.Handler aiHandler =
            new android.os.Handler(android.os.Looper.getMainLooper());
    private android.os.Handler colorHandler = new android.os.Handler();

    private Runnable aiTicker, aiRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        turnAlert = findViewById(R.id.turnAlert);
        board = findViewById(R.id.board);

        Intent settingIntent = getIntent();
        boardSize = settingIntent.getIntExtra("boardSize", 3);
        infinite = settingIntent.getBooleanExtra("infiniteMode", false);
        gamble = settingIntent.getBooleanExtra("gambleMode", false);
        shuffle = settingIntent.getBooleanExtra("shuffleMode", false);
        single = settingIntent.getBooleanExtra("singlePlayer", false);
        ultimate = settingIntent.getBooleanExtra("ultimateTTT", false);

        state = new GameState();
        state.boardSize = boardSize;
        state.board = new String[boardSize][boardSize];
        engine = new GameEngine(state);
        engine.setModes(
                infinite,
                gamble,
                shuffle,
                single,
                ultimate
        );
        if (ultimate) {
            state.ultimateBoards = new String[3][3][3][3];
            state.ultimateMetaBoard = new String[3][3];
            updateUltimateTurnText();
            loadOuterBoard();
            startBoardColorGradient();
        }
        aiThinking = false;
        aiDotCount = 0;
        p1Score = findViewById(R.id.p1Score_TextView);
        p2Score = findViewById(R.id.p2Score_TextView);
        buildBoard();
        updateTurn();
        updateScoreUI();



        Button endGameButton = findViewById(R.id.endGame_Button);
        endGameButton.setOnClickListener(v -> {
            // reset scores
            state.xScore = 0;
            state.oScore = 0;
            // reset board state
            clearBoardUI();
            // go back to main menu
            Intent homeIntent = new Intent(Menu.this, MainActivity.class);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);

            finish(); // closes game activity
        });
    }

    public void refresh() {

        for (int i = 0; i < board.getChildCount(); i++) {
            Button b = (Button) board.getChildAt(i);
            int r = i / boardSize;
            int c = i % boardSize;
            String val = state.board[r][c];

            b.setText(val == null ? "" : val);
            b.setTextColor(getPlayerColor(val));
            applyPieceGlow(b, val);
        }
        if (!aiThinking) {
            updateTurn();
        }
    }
    public void buildBoard() {
        board.removeAllViews();
        board.setRowCount(boardSize);
        board.setColumnCount(boardSize);
        for (int i = 0; i < boardSize * boardSize; i++) {
            int r = i / boardSize;
            int c = i % boardSize;
            Button btn = new Button(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.rowSpec = GridLayout.spec(r, 1f);
            params.columnSpec = GridLayout.spec(c, 1f);
            params.width = 0;
            params.height = 0;
            params.setGravity(Gravity.FILL);


            btn.setLayoutParams(params);
            btn.setTextSize(30);
            btn.setGravity(Gravity.CENTER);
            btn.setBackgroundResource(R.drawable.grid_cell);
            btn.setBackgroundTintList(null);
            btn.setPadding(0, 0, 0, 0);
            btn.setIncludeFontPadding(false);

            btn.setOnClickListener(v -> {
                if (gameOver) return;
                if (ultimate) {
                    handleUltimateClick(r, c);
                    return;
                }

                Object result = engine.playMove(r, c);
                refresh();
                updateTurn();

                if (engine.singlePlayer && !state.xTurn) {
                    startAiThinking();

                    // simulates thinking delay for fun
                }

                if (result instanceof int[][]) {
                    int[][] winCells = (int[][]) result;
                    if ("X".equals(state.lastMovePlayer)) {
                        state.xScore++;
                    } else {
                        state.oScore++;
                    }

                    updateScoreUI();
                    animateWinLine(winCells);

                    new android.os.Handler().postDelayed(() -> {
                        engine.reset();
                        clearBoardUI();
                        refresh();
                        updateTurn();
                    }, 500);
                }


                else if (result instanceof String && result.equals("DRAW")) {
                    showDrawAnimation();
                }
            });
            board.addView(btn);
        }
    }
    private void updateTurn() {

        String player = state.xTurn ? "X" : "O";

        int textColor;

        if (state.xTurn) {
            textColor = 0xFFFF4444; // red for X
        } else {
            textColor = 0xFFB388FF; // purple for O (fixed)
        }

        String text;

        if (ultimate && currentBoardRow != -1) {
            text = player + " Turn - (" + (currentBoardRow + 1) + "," + (currentBoardCol + 1) + ")";
        } else if (ultimate) {
            text = player + " Turn - Choose a board";
        } else {
            text = player + " Turn";
        }

        turnAlert.setText(text);
        turnAlert.setTextColor(textColor);
        turnAlert.setTextSize(40);

        if (!state.xTurn) {
            turnAlert.setShadowLayer(8f, 0f, 0f, 0xFFB388FF);
        } else {
            turnAlert.setShadowLayer(0f, 0f, 0f, 0x00000000);
        }

        if (!ultimate) {
            int bgColor = state.xTurn
                    ? 0xFF101010   // X dark theme
                    : 0xFF2A0000;  // O dark red/purple-ish tone
            board.setBackgroundColor(bgColor);
        }

    }
    private void updateScoreUI() {
        p1Score.setText("Player 1: " + toTally(state.xScore));
        p2Score.setText("Player 2: " + toTally(state.oScore));
    }
    private void clearBoardUI() {

        // clear buttons visually
        for (int i = 0; i < board.getChildCount(); i++) {
            Button b = (Button) board.getChildAt(i);
            b.setText("");
        }

        // clear logical board
        for (int r = 0; r < state.boardSize; r++) {
            for (int c = 0; c < state.boardSize; c++) {
                state.board[r][c] = null;
            }
        }

        // ONLY reset infinite mode tracking
        if (infinite) {
            state.moveHistory.clear();
            state.turnCount = 0;
        }
    }
    private String toTally(int score) {
        StringBuilder sb = new StringBuilder();

        int groupsOfFive = score / 5;
        int remainder = score % 5;

        for (int i = 0; i < groupsOfFive; i++) {
            sb.append("||||/ ");
        }

        for (int i = 0; i < remainder; i++) {
            sb.append("|");
        }

        return sb.toString().trim();
    }

    // AI code
    private void startAiThinking() {
        aiThinking = true;
        aiDotCount = 0;
        aiHandler.removeCallbacks(aiRunnable);

        aiRunnable = new Runnable() {
            @Override
            public void run() {

                if (!aiThinking) return;

                String[] states = {
                        "AI thinking",
                        "AI thinking .",
                        "AI thinking ..",
                        "AI thinking ..."
                };
                turnAlert.setTextColor(0xFF000000);
                turnAlert.setText(states[aiDotCount % 4]);

                aiDotCount++;

                aiHandler.postDelayed(this, 500);
            }
        };

        aiHandler.post(aiRunnable);

        int delay = 1500 + (int)(Math.random() * 2500);

        aiHandler.postDelayed(() -> stopAiThinkingAndMove(), delay);
    }
    private void stopAiThinkingAndMove() {

        aiThinking = false;

        aiHandler.removeCallbacks(aiRunnable);

        turnAlert.setText("AI ready");

        int[] move = engine.getAIMove();

        if (move != null) {
            engine.playMove(move[0], move[1]);
            refresh();
            updateTurn();
        }
    }

    //UltimateTTT Code
    private int currentBoardRow = -1;
    private int currentBoardCol = -1;

    //View creators
    private void loadMiniBoard(int br, int bc) {
        int tint = getMiniBoardTint(br, bc);
        board.removeAllViews();
        board.setRowCount(3);
        board.setColumnCount(3);
        String[][] mini = state.ultimateBoards[br][bc];

        for (int i = 0; i < 9; i++) {
            int r = i / 3;
            int c = i % 3;
            Button btn = new Button(this);
            btn.setTextSize(32);
            btn.setTypeface(null, android.graphics.Typeface.BOLD);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.rowSpec = GridLayout.spec(r, 1f);
            params.columnSpec = GridLayout.spec(c, 1f);
            params.width = 0;
            params.height = 0;
            btn.setLayoutParams(params);
            btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(tint));

            btn.setText(mini[r][c] == null ? "" : mini[r][c]);
            btn.setTextColor(getPlayerColor(mini[r][c]));
            applyPieceGlow(btn, mini[r][c]);
            int finalR = r;
            int finalC = c;
            btn.setOnClickListener(v -> handleUltimateClick(finalR, finalC));
            board.addView(btn);
        }
    }
    private void loadOuterBoard() {
        board.removeAllViews();
        board.setRowCount(3);
        board.setColumnCount(3);
        for (int i = 0; i < 9; i++) {
            int r = i / 3;
            int c = i % 3;
            Button btn = new Button(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.rowSpec = GridLayout.spec(r, 1f);
            params.columnSpec = GridLayout.spec(c, 1f);
            params.width = 0;
            params.height = 0;

            btn.setLayoutParams(params);
            btn.setTextSize(36);
            btn.setTypeface(null, android.graphics.Typeface.BOLD);

            String val = state.ultimateMetaBoard[r][c];

            btn.setText(val == null ? "" : val);
            btn.setTextColor(getPlayerColor(val));
            applyPieceGlow(btn, val); // 👈 ADD HERE
            int finalR = r;
            int finalC = c;
            btn.setOnClickListener(v -> handleUltimateClick(finalR, finalC));
            board.addView(btn);
        }
    }
    //gameplay Managers
    private void handleUltimateClick(int r, int c) {
        if (gameOver) return;
        // selecting mini board
        if (currentBoardRow == -1) {
            if (state.ultimateMetaBoard[r][c] != null) return;
            currentBoardRow = r;
            currentBoardCol = c;
            updateUltimateTurnText();
            loadMiniBoard(r, c);
            updateTurn();
            return;
        }

        // playing inside mini board
        String[][] mini = state.ultimateBoards[currentBoardRow][currentBoardCol];
        if (mini[r][c] != null) return;
        String player = state.xTurn ? "X" : "O";
        mini[r][c] = player;
        refreshMiniBoard(mini);

        // check win
        if (engine.checkMiniWin(mini, player)) {
            state.ultimateMetaBoard[currentBoardRow][currentBoardCol] = player;
            startBoardColorGradient();

            showBoardWonFlash(player);

            // return to outer board
            currentBoardRow = -1;
            currentBoardCol = -1;

            updateUltimateTurnText();
            loadOuterBoard();

            if (engine.checkUltimateWin(player)) {
                turnAlert.setTextSize(35);
                endGame(player + " WINS ULTIMATE (3 IN A ROW)");

                String tieResult = resolveUltimateTie();

                if (tieResult != null) {
                    if (tieResult.equals("DRAW")) {
                        turnAlert.setText("ULTIMATE DRAW");
                    } else {
                        endGame(tieResult + " WINS BY CONTROL");
                    }
                }
                return;


            }
        }

        // reset board if draw until win
        if (isMiniDraw(mini)) {
            state.ultimateBoards[currentBoardRow][currentBoardCol] = new String[3][3];
            refreshMiniBoard(state.ultimateBoards[currentBoardRow][currentBoardCol]);
            updateUltimateTurnText();
        }

        state.xTurn = !state.xTurn;
        updateTurn();
        updateUltimateTurnText();

    }
    private void refreshMiniBoard(String[][] mini) {
        for (int i = 0; i < board.getChildCount(); i++) {
            Button b = (Button) board.getChildAt(i);
            int r = i / 3;
            int c = i % 3;
            b.setText(mini[r][c] == null ? "" : mini[r][c]);
            b.setTextColor(getPlayerColor(mini[r][c]));
            b.setShadowLayer(6f, 0f, 0f, 0xFF000000);
        }
    }
    private boolean isMiniDraw(String[][] mini) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (mini[i][j] == null) return false;
            }
        }
        return true;
    }
    private void updateUltimateTurnText() {

        String player = state.xTurn ? "X" : "O";

        // color logic
        int textColor = state.xTurn ? 0xFFFF4444 : 0xFF000000;
        turnAlert.setTextColor(textColor);

        if (currentBoardRow == -1) {
            turnAlert.setText(player + " Turn - Choose a board");
        } else {
            int displayRow = currentBoardRow + 1;
            int displayCol = currentBoardCol + 1;

            turnAlert.setTextSize(40);
            turnAlert.setText(player + " Turn - \nPlaying in (" + displayRow + ", " + displayCol + ")");
        }
    }
    private String resolveUltimateTie() {
        int x = 0;
        int o = 0;
        boolean full = true;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {

                String v = state.ultimateMetaBoard[i][j];

                if (v == null) {
                    full = false;
                } else if (v.equals("X")) {
                    x++;
                } else if (v.equals("O")) {
                    o++;
                }
            }
        }

        if (!full) return null;
        if (x > o) return "X";
        if (o > x) return "O";
        return "DRAW";
    }
    private void endGame(String resultText) {
        gameOver = true;
        turnAlert.setText(resultText);
        turnAlert.setTextColor(0xFFFFFFFF);
        // freezes board visually
        for (int i = 0; i < board.getChildCount(); i++) {
            board.getChildAt(i).setEnabled(false);
        }
        board.animate()
                .alpha(0.6f)
                .setDuration(400);
    }

    // Animation/Asthetic Codes
    private void animateWinLine(int[][] winCells) {
        android.os.Handler handler = new android.os.Handler();
        for (int i = 0; i < winCells.length; i++) {
            int delay = i * 90; // sweep speed
            int r = winCells[i][0];
            int c = winCells[i][1];
            handler.postDelayed(() -> {
                int index = r * boardSize + c;
                Button b = (Button) board.getChildAt(index);
                if (b == null) return;
                b.setAlpha(1f);
                b.animate()
                        .alpha(0.6f)
                        .setDuration(120)
                        .withEndAction(() ->
                                b.animate().alpha(1f).setDuration(120)
                        );

            }, delay);
        }
    }
    private void showDrawAnimation() {

        for (int i = 0; i < board.getChildCount(); i++) {
            Button b = (Button) board.getChildAt(i);

            b.setText("DRAW");
            b.setShadowLayer(6f, 0f, 0f, 0xFF000000);
            b.setAlpha(1f);

            b.animate()
                    .alpha(0.3f)
                    .setDuration(200)
                    .withEndAction(() ->
                            b.animate()
                                    .alpha(1f)
                                    .setDuration(200)
                    );
        }

        new android.os.Handler().postDelayed(() -> {
            engine.reset();
            clearBoardUI();
            refresh();
        }, 1000);
    }
    private void showBoardWonFlash(String player) {
        turnAlert.setText(player + " WON THE BOARD!");
        int flashColor = player.equals("X") ? 0xFFFF4444 : 0xFF000000;
        turnAlert.setTextColor(flashColor);
        turnAlert.setScaleX(1f);
        turnAlert.setScaleY(1f);
        turnAlert.animate()
                .scaleX(1.3f)
                .scaleY(1.3f)
                .setDuration(150)
                .withEndAction(() ->
                        turnAlert.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(150)
                );
        new android.os.Handler().postDelayed(() -> {
            updateUltimateTurnText();
        }, 800);
    }
    private int getMiniBoardTint(int r, int c) {

        int base = 55; // dark base

        int offset = (r * 3 + c) * 3;

        int red = base + offset;
        int green = base;
        int blue = base;

        return android.graphics.Color.rgb(
                Math.min(red, 50),
                Math.min(green, 40),
                Math.min(blue, 40)
        );
    }
    private void startBoardColorGradient() {

        colorHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                int xCount = 0;
                int oCount = 0;

                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        String v = state.ultimateMetaBoard[i][j];
                        if ("X".equals(v)) xCount++;
                        if ("O".equals(v)) oCount++;
                    }
                }

                float total = Math.max(1, xCount + oCount);
                float balance = (xCount - oCount) / total;

                float targetRed = 35 + (balance * 90);
                float targetGreen = 25 + (Math.abs(balance) * 10);
                float targetBlue = 25 + (Math.abs(balance) * 10);

                // clamp
                targetRed = clamp(targetRed, 0, 120);
                targetGreen = clamp(targetGreen, 0, 60);
                targetBlue = clamp(targetBlue, 0, 60);

                // gradient over time
                currentRed += (targetRed - currentRed) * 0.08f;
                currentGreen += (targetGreen - currentGreen) * 0.08f;
                currentBlue += (targetBlue - currentBlue) * 0.08f;

                int color = android.graphics.Color.rgb(
                        (int) currentRed,
                        (int) currentGreen,
                        (int) currentBlue
                );

                board.setBackgroundColor(color);

                colorHandler.postDelayed(this, 50);
            }
        }, 50);
    }
    private float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
    private int getPlayerColor(String value) {
        if ("X".equals(value)) return 0xFFFF4444; // red
        if ("O".equals(value)) return 0xFFB388FF; // black
        return 0xFFFFFFFF;
    }
    private void applyPieceGlow(Button b, String value) {

        if ("O".equals(value)) {
            b.setShadowLayer(8f, 0f, 0f, 0xFFB388FF);
        } else if ("X".equals(value)) {
            b.setShadowLayer(8f, 0f, 0f, 0xFFFF4444);
        } else {
            b.setShadowLayer(0f, 0f, 0f, 0x00000000);
        }
    }

}
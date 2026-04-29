package com.byers.toetictac;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Menu extends AppCompatActivity {

    GameState state;
    GameEngine engine;

    GridLayout board;
    FrameLayout zoomContainer;

    TextView turnAlert, p1Score, p2Score;

    int boardSize, bgColor, textColor;
    int aiDotCount;
    int activeRow = -1, activeCol = -1;

    boolean infinite, gamble, shuffle, single, ultimate, aiThinking;

    android.os.Handler aiHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    Runnable aiRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        turnAlert = findViewById(R.id.turnAlert);
        board = findViewById(R.id.board);
        zoomContainer = findViewById(R.id.zoomContainer);

        Intent intent = getIntent();

        boardSize = intent.getIntExtra("boardSize", 3);
        infinite = intent.getBooleanExtra("infiniteMode", false);
        gamble = intent.getBooleanExtra("gambleMode", false);
        shuffle = intent.getBooleanExtra("shuffleMode", false);
        single = intent.getBooleanExtra("singlePlayer", false);
        ultimate = intent.getBooleanExtra("ultimateTTT", false);

        state = new GameState();
        state.boardSize = boardSize;
        state.board = new String[boardSize][boardSize];

        engine = new GameEngine(state);
        engine.setModes(infinite, gamble, shuffle, single, ultimate);

        if (ultimate) {
            state.ultimateBoards = new String[boardSize][boardSize][3][3];
            state.ultimateMetaBoard = new String[boardSize][boardSize];
        }

        if (single) {
            state.xTurn = Math.random() < 0.5;
            engine.aiIsX = !state.xTurn;
        }

        zoomContainer.setOnClickListener(v -> closeZoomBoard());

        p1Score = findViewById(R.id.p1Score_TextView);
        p2Score = findViewById(R.id.p2Score_TextView);

        buildBoard();
        updateTurn();
        updateScoreUI();

        if (single && state.xTurn == engine.aiIsX) {
            aiHandler.postDelayed(this::startAiThinking, 500);
        }

        findViewById(R.id.endGame_Button).setOnClickListener(v -> {
            state.xScore = 0;
            state.oScore = 0;
            clearBoardUI();

            Intent home = new Intent(Menu.this, MainActivity.class);
            home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(home);
            finish();
        });
    }

    // ================= BOARD =================

    public void refresh() {
        for (int i = 0; i < board.getChildCount(); i++) {
            Button b = (Button) board.getChildAt(i);
            int r = i / boardSize;
            int c = i % boardSize;
            b.setText(state.board[r][c] == null ? "" : state.board[r][c]);
        }
        if (!aiThinking) updateTurn();
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
            btn.setTextSize(22);
            btn.setGravity(Gravity.CENTER);
            btn.setBackgroundResource(R.drawable.grid_cell);

            btn.setOnClickListener(v -> {

                if (ultimate) {
                    if (zoomContainer.getVisibility() == View.VISIBLE) return;
                    openZoomBoard(r, c);
                    return;
                }

                Object result = engine.playMove(r, c);
                refresh();

                handleResult(result);
            });

            board.addView(btn);
        }
    }

    // ================= RESULT HANDLER =================

    private void handleResult(Object result) {

        if (result == null) return;

        if (result instanceof int[][]) {

            int[][] win = (int[][]) result;

            if ("X".equals(state.lastMovePlayer)) state.xScore++;
            else state.oScore++;

            updateScoreUI();
            animateWinLine(win);

            aiHandler.postDelayed(() -> {
                engine.reset();
                clearBoardUI();
                refresh();
            }, 500);

        } else if ("DRAW".equals(result)) {

            showDrawAnimation();
        }
    }

    // ================= TURN =================

    public void updateTurn() {
        if (aiThinking) return;

        boolean xTurn = state.xTurn;

        turnAlert.setText(xTurn ? "X Turn" : "O Turn");

        bgColor = xTurn ? 0xFF101010 : 0xFF2A0000;
        textColor = xTurn ? 0xFFFF4D4D : 0xFFFFFFFF;

        turnAlert.setTextColor(textColor);
        board.setBackgroundColor(bgColor);
    }

    private void updateScoreUI() {
        p1Score.setText("Player 1: " + state.xScore);
        p2Score.setText("Player 2: " + state.oScore);
    }

    private void clearBoardUI() {
        for (int i = 0; i < board.getChildCount(); i++) {
            ((Button) board.getChildAt(i)).setText("");
        }

        for (int r = 0; r < state.boardSize; r++) {
            for (int c = 0; c < state.boardSize; c++) {
                state.board[r][c] = null;
            }
        }

        if (infinite) {
            state.moveHistory.clear();
            state.turnCount = 0;
        }
    }

    // ================= AI =================

    private void startAiThinking() {

        if (ultimate) return;

        aiThinking = true;
        aiDotCount = 0;

        aiHandler.removeCallbacks(aiRunnable);

        aiRunnable = new Runnable() {
            @Override
            public void run() {
                if (!aiThinking) return;

                String[] s = {
                        "AI thinking",
                        "AI thinking .",
                        "AI thinking ..",
                        "AI thinking ..."
                };

                turnAlert.setText(s[aiDotCount % 4]);
                aiDotCount++;

                aiHandler.postDelayed(this, 500);
            }
        };

        aiHandler.post(aiRunnable);

        int delay = 1500 + (int)(Math.random() * 2500);
        aiHandler.postDelayed(this::stopAiThinkingAndMove, delay);
    }

    private void stopAiThinkingAndMove() {
        aiThinking = false;
        aiHandler.removeCallbacks(aiRunnable);

        int[] move = engine.getAIMove();
        if (move != null) {
            engine.playMove(move[0], move[1]);
            refresh();
        }
    }

    // ================= ULTIMATE MODE =================

    private void openZoomBoard(int br, int bc) {

        state.activeRow = br;
        state.activeCol = bc;
        if (state.ultimateBoards[br][bc] == null) {
            state.ultimateBoards[br][bc] = new String[3][3];
        }
        final String[][] miniBoard = state.ultimateBoards[br][bc];
        turnAlert.setTextSize(35);
        turnAlert.setText("Playing in cell: " + br + ", " + bc);

        board.animate()
                .alpha(0.25f)
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(200);

        zoomContainer.removeAllViews();
        zoomContainer.setVisibility(View.VISIBLE);

        GridLayout mini = new GridLayout(this);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        lp.gravity = Gravity.CENTER;
        mini.setLayoutParams(lp);
        mini.setRowCount(3);
        mini.setColumnCount(3);

        for (int i = 0; i < 9; i++) {
            final int rr = i / 3;
            final int cc = i % 3;

            Button b = new Button(this);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 220;
            params.height = 220;
            params.setMargins(8, 8, 8, 8);
            params.setGravity(Gravity.CENTER);
            b.setLayoutParams(params);

            b.setText(miniBoard[rr][cc] == null ? "" : miniBoard[rr][cc]);

            b.setTextColor(0xFF000000);
            b.setBackgroundColor(0xFFE0E0E0);
            b.setAlpha(1f);

            b.setOnClickListener(v -> {
                android.util.Log.d("UTTT", "MiniBoard[" + br + "][" + bc + "] = " + miniBoard);

                String player = state.xTurn ? "X" : "O";

                miniBoard[rr][cc] = player;

                b.setText(player);
                b.invalidate();
                b.requestLayout();

                // IMPORTANT: update meta BEFORE closing
                if (checkMiniWin(miniBoard, player)) {
                    state.ultimateMetaBoard[br][bc] = player;
                }

                state.xTurn = !state.xTurn;

                refreshUltimate();
                closeZoomBoard();
            });

            mini.addView(b);
        }

        lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        lp.gravity = Gravity.CENTER;

        mini.setLayoutParams(lp);

        mini.setScaleX(0.6f);
        mini.setScaleY(0.6f);
        mini.setAlpha(0f);

        mini.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(200);

        zoomContainer.addView(mini);
    }

    private void closeZoomBoard() {

        zoomContainer.setVisibility(View.GONE);
        zoomContainer.removeAllViews();

        board.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(200);
    }
    public void refreshUltimate() {

        for (int i = 0; i < board.getChildCount(); i++) {

            Button b = (Button) board.getChildAt(i);

            int r = i / boardSize;
            int c = i % boardSize;

            String winner = state.ultimateMetaBoard[r][c];

            b.setText(winner == null ? "" : winner);

            // reset visuals
            b.setAlpha(1f);
            b.setScaleX(1f);
            b.setScaleY(1f);
            b.setBackgroundResource(R.drawable.grid_cell);

            // highlight active meta-cell
            if (r == state.activeRow && c == state.activeCol) {
                b.setBackgroundResource(R.drawable.active_cell_glow);
                b.setAlpha(1f);
                b.setScaleX(1.08f);
                b.setScaleY(1.08f);
            } else {
                b.setAlpha(0.6f);
            }
        }

        updateTurn();
    }

    // ================= VISUAL =================

    private void animateWinLine(int[][] cells) {
        android.os.Handler h = new android.os.Handler();

        for (int i = 0; i < cells.length; i++) {
            int delay = i * 90;
            int r = cells[i][0];
            int c = cells[i][1];

            h.postDelayed(() -> {
                int index = r * boardSize + c;
                Button b = (Button) board.getChildAt(index);

                if (b == null) return;

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
        }

        aiHandler.postDelayed(() -> {
            engine.reset();
            clearBoardUI();
            refresh();
        }, 1000);
    }

    // placeholder
    private boolean checkMiniWin(String[][] b, String p) {
        return false;
    }
}
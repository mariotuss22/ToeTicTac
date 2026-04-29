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
    boolean infinite, gamble, shuffle, single, ultimate, aiThinking;
    private android.os.Handler aiHandler =
            new android.os.Handler(android.os.Looper.getMainLooper());
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
            btn.setTextSize(22);
            btn.setGravity(Gravity.CENTER);
            btn.setBackgroundResource(R.drawable.grid_cell);
            btn.setBackgroundTintList(null);
            btn.setPadding(0, 0, 0, 0);
            btn.setIncludeFontPadding(false);

            btn.setOnClickListener(v -> {
                Object result = engine.playMove(r, c);
                refresh();

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
                    }, 500);
                    return;
                }


                else if (result instanceof String && result.equals("DRAW")) {
                    showDrawAnimation();
                }
            });
            board.addView(btn);
        }
    }


    public void updateTurn() {

        if (aiThinking) {
            // AI owns the UI right now — do nothing
            return;
        }

        boolean xTurn = state.xTurn;

        turnAlert.setText(xTurn ? "X Turn" : "O Turn");

        bgColor = xTurn ? 0xFF101010 : 0xFF2A0000;
        textColor = xTurn ? 0xFFFF4D4D : 0xFFFFFFFF;

        turnAlert.setTextColor(textColor);

        for (int i = 0; i < board.getChildCount(); i++) {
            Button b = (Button) board.getChildAt(i);
            b.setTextColor(textColor);
        }

        board.setBackgroundColor(bgColor);
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
        }
    }

    // kinda works for now
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

}
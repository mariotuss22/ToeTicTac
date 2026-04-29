package com.byers.toetictac;

import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.ArrayList;
import java.util.Random;

public class settingsActivity extends AppCompatActivity {

    public Switch inf_Switch, gamble_Switch, shuffle_Switch, single_Switch, ultimate_Switch;
    public Button finish_button;
    public ArrayList<Button> previewButtons = new ArrayList<>();
    public GridLayout preview_board;
    public int boardSize,switchy;
    public SeekBar boardSize_Slider;
    public TextView boardSize_txtView;
    public int sillySwitches;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        inf_Switch = findViewById(R.id.inf_Switch);
        gamble_Switch = findViewById(R.id.gamble_Switch);

        gamble_Switch.setOnCheckedChangeListener((b, isChecked) -> applyModeRules());

        shuffle_Switch = findViewById(R.id.shuffle_Switch);

        shuffle_Switch.setOnCheckedChangeListener((b, isChecked) -> applyModeRules());

        ultimate_Switch = findViewById(R.id.ultimate_Switch);

        ultimate_Switch.setOnCheckedChangeListener((b, isChecked) -> applyModeRules());

        single_Switch = findViewById(R.id.single_Switch);
        preview_board = findViewById(R.id.preview_board);
        boardSize_Slider = findViewById(R.id.boardSize_Slider);
        finish_button = findViewById(R.id.finish_button);
        boardSize_txtView = findViewById(R.id.boardSize_txtView);



        finish_button.setOnClickListener(v -> {
                    Intent intent = new Intent(this, Menu.class);
                    intent.putExtra("boardSize", boardSize);
                    intent.putExtra("infiniteMode", inf_Switch.isChecked());
                    intent.putExtra("gambleMode", gamble_Switch.isChecked());
                    intent.putExtra("shuffleMode", shuffle_Switch.isChecked());
                    intent.putExtra("singlePlayer", single_Switch.isChecked());
                    intent.putExtra("ultimateTTT", ultimate_Switch.isChecked());
                    startActivity(intent);
                });



        //slider starts at 3 (a regular board) and goes up to 9 (a 9x9 board) or down to 1 (a 1x1 joke board)
        boardSize_Slider.setMax(8);
        boardSize_Slider.setProgress(2); // default = 3x3
        boardSize = 3; // default
        boardSize_txtView.setText("" + boardSize + "x" + boardSize);
        boardSize_Slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                boardSize = progress + 1;
                boardSize_txtView.setText("Board Size: " + boardSize + "x" + boardSize);
                previewMaker();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        previewMaker();
    }
    public void previewMaker() {

        preview_board.post(() -> {

            preview_board.removeAllViews();

            int width = preview_board.getWidth();
            int height = preview_board.getHeight();

            int cellSize = Math.min(width, height) / boardSize;

            preview_board.setRowCount(boardSize);
            preview_board.setColumnCount(boardSize);

            for (int i = 0; i < boardSize * boardSize; i++) {

                Button btn = new Button(this);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();

                params.width = cellSize;
                params.height = cellSize;

                params.rowSpec = GridLayout.spec(i / boardSize);
                params.columnSpec = GridLayout.spec(i % boardSize);

                btn.setLayoutParams(params);

                // styling fixes
                btn.setBackgroundResource(R.drawable.grid_cell);
                btn.setPadding(0, 0, 0, 0);
                btn.setMinHeight(0);
                btn.setMinWidth(0);
                btn.setTextSize(18);
                btn.setGravity(Gravity.CENTER);

                styleButton(btn, i);

                preview_board.addView(btn);
            }
        });
    }
    Random random = new Random();
    private void styleButton(Button btn, int index) {

        btn.setAllCaps(false);
        btn.setIncludeFontPadding(false);
        btn.setGravity(Gravity.CENTER);

        btn.setText(random.nextBoolean() ? "X" : "O");

        btn.setTextSize(16);
        btn.setBackgroundResource(R.drawable.grid_cell);

        btn.setOnClickListener(v -> {

            String current = btn.getText().toString();
            btn.setText(current.equals("X") ? "O" : "X");
        });
    }

    //restrictions
    private void applyModeRules() {
        // gamble/shuffle are mutually exclusive
        if (gamble_Switch.isChecked() && shuffle_Switch.isChecked()) {
            if (switchy == 0) {
                gamble_Switch.setChecked(false);
                switchy++;
            } else {
                shuffle_Switch.setChecked(false);
                switchy--;
            }
            if (sillySwitches ==  0) {
                Toast.makeText(this,"Shuffle & Gamble are mutually exclusive", Toast.LENGTH_SHORT).show();
                sillySwitches++;
            } else {
                showModeSass();
            }


        }

        //if ult is on, nothing else is
        if (ultimate_Switch.isChecked()) {
            Toast.makeText(this,"UltimateTTT disables other modes", Toast.LENGTH_SHORT).show();
            inf_Switch.setChecked(false);
            gamble_Switch.setChecked(false);
            shuffle_Switch.setChecked(false);
            single_Switch.setChecked(false);

            boardSize = 3;
            boardSize_Slider.setProgress(2);
            boardSize_Slider.setEnabled(false);
            boardSize_txtView.setText("3x3 Locked");

            return;
        } else {
            boardSize_Slider.setEnabled(true);
        }
    }
    private void showModeSass() {
        Random random = new Random();
        int rand = random.nextInt(9);
        String[] insults = {
                "Nope. Pick one.",
                "They're not compatible 😐",
                "You're really trying this again?",
                "Stop it.",
                "I'm judging you.",
                "Fine. I'll fix it for you... again.",
                "Cease this behavior",
                "what is your deal?",
                "you're persistent",
                "is this what you do for fun?"
        };
        int index = Math.min(rand, insults.length - 1);
        Toast.makeText(this, insults[index], Toast.LENGTH_SHORT).show();
        sillySwitches++;
    }






}
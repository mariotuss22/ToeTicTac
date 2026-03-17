package com.byers.toetictac;

import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class settingsActivity extends AppCompatActivity {

    public Switch inf_Switch, gamble_Switch, shuffle_Switch, single_Switch;
    public ArrayList<Button> previewButtons = new ArrayList<>();
    public View preview_board;

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
        shuffle_Switch = findViewById(R.id.shuffle_Switch);
        single_Switch = findViewById(R.id.single_Switch);

    }
}
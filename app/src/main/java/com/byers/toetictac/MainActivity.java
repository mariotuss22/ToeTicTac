package com.byers.toetictac;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        TextView titleTextView = findViewById(R.id.titleTextView);
        Button startButton = findViewById(R.id.start_Button);
        Button settingsButton = findViewById(R.id.settings_Button);



        startButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Menu.class);
            startActivity(intent);
        });
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, settingsActivity.class);
            startActivity(intent);
        });


    }





}
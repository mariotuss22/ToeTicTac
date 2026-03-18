package com.byers.toetictac;

import static java.util.Calendar.getInstance;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;

public class Menu extends AppCompatActivity {

    boolean player1Turn = false;
    boolean player2Turn = true;
    TextView turnAlert;
    public ArrayList<Button> XOButtons = new ArrayList<>();
    Resources res;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button button0 = findViewById(R.id.button0);
        Button button1 = findViewById(R.id.button1);
        Button button2 = findViewById(R.id.button2);
        Button button3 = findViewById(R.id.button3);
        Button button4 = findViewById(R.id.button4);
        Button button5 = findViewById(R.id.button5);
        Button button6 = findViewById(R.id.button6);
        Button button7 = findViewById(R.id.button7);
        Button button8 = findViewById(R.id.button8);
        TextView turnAlert = findViewById(R.id.turnAlert);

        initializeButtons();


//        button0.setOnClickListener(new View.OnClickListener() {
//            @Override
//                    public void onClick(View v) {
//                    if (player1Turn) {
//                        button0.setText("X");
//                        button0.setClickable(false);
//                        player2Turn = true;
//                        player1Turn = false;
//                        changeTurnAlert(turnAlert);
//                } else if (player2Turn) {
//                        button0.setText("o");
//                        button0.setClickable(false);
//                        player1Turn = true;
//                        player2Turn = false;
//                        changeTurnAlert(turnAlert);
//                    }
//            }
//
//        });
//        button1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (player1Turn) {
//                    button1.setText("X");
//                    button1.setClickable(false);
//                    player2Turn = true;
//                    player1Turn = false;
//                    changeTurnAlert(turnAlert);
//                } else if (player2Turn) {
//                    button1.setText("o");
//                    button1.setClickable(false);
//                    player1Turn = true;
//                    player2Turn = false;
//                    changeTurnAlert(turnAlert);
//                }
//            }
//
//        });
//        button2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (player1Turn) {
//                    button2.setText("X");
//                    button2.setClickable(false);
//                    player2Turn = true;
//                    player1Turn = false;
//                    changeTurnAlert(turnAlert);
//                } else if (player2Turn) {
//                    button2.setText("o");
//                    button2.setClickable(false);
//                    player1Turn = true;
//                    player2Turn = false;
//                    changeTurnAlert(turnAlert);
//                }
//            }
//
//        });
//        button3.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (player1Turn) {
//                    button3.setText("X");
//                    button3.setClickable(false);
//                    player2Turn = true;
//                    player1Turn = false;
//                    changeTurnAlert(turnAlert);
//
//                } else if (player2Turn) {
//                    button3.setText("o");
//                    button3.setClickable(false);
//                    player1Turn = true;
//                    player2Turn = false;
//                    changeTurnAlert(turnAlert);
//                }
//            }
//
//        });
//
//        button4.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (player1Turn) {
//                    button4.setText("X");
//                    button4.setClickable(false);
//                    player2Turn = true;
//                    player1Turn = false;
//                    changeTurnAlert(turnAlert);
//                } else if (player2Turn) {
//                    button4.setText("o");
//                    button4.setClickable(false);
//                    player1Turn = true;
//                    player2Turn = false;
//                    changeTurnAlert(turnAlert);
//                }
//            }
//
//        });
//
//        button5.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (player1Turn) {
//                    button5.setText("X");
//                    button5.setClickable(false);
//                    player2Turn = true;
//                    player1Turn = false;
//                    changeTurnAlert(turnAlert);
//                } else if (player2Turn) {
//                    button5.setText("o");
//                    button5.setClickable(false);
//                    player1Turn = true;
//                    player2Turn = false;
//                    changeTurnAlert(turnAlert);
//                }
//            }
//
//        });
//
//        button6.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (player1Turn) {
//                    button6.setText("X");
//                    button6.setClickable(false);
//                    player2Turn = true;
//                    player1Turn = false;
//                    changeTurnAlert(turnAlert);
//                } else if (player2Turn) {
//                    button6.setText("o");
//                    button6.setClickable(false);
//                    player1Turn = true;
//                    player2Turn = false;
//                    changeTurnAlert(turnAlert);
//                }
//            }
//
//        });
//
//        button7.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (player1Turn) {
//                    button7.setText("X");
//                    button7.setClickable(false);
//                    player2Turn = true;
//                    player1Turn = false;
//                    changeTurnAlert(turnAlert);
//                } else if (player2Turn) {
//                    button7.setText("o");
//                    button7.setClickable(false);
//                    player1Turn = true;
//                    player2Turn = false;
//                    changeTurnAlert(turnAlert);
//                }
//            }
//
//        });
//
//        button8.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (player1Turn) {
//                    button8.setText("X");
//                    button8.setClickable(false);
//                    player2Turn = true;
//                    player1Turn = false;
//                    changeTurnAlert(turnAlert);
//
//                } else if (player2Turn) {
//                    button8.setText("o");
//                    button8.setClickable(false);
//                    player1Turn = true;
//                    player2Turn = false;
//                    changeTurnAlert(turnAlert);
//                }
//            }
//
//        });

    }

    public void initializeButtons() {
        String buttonID;
        res = getResources();
        res = getResources();



        for (int i = 0; i < 8; i++) {
            buttonID = "button" + i;
            int resID = res.getIdentifier(buttonID, "id", getPackageName());
            XOButtons.add(findViewById(resID));
            XOButtons.get(i).setVisibility(View.VISIBLE);
            XOButtons.get(i).setPadding(5, 5, 5, 5);
            XOButtons.get(i).setText(String.valueOf(i + 1));


            XOButtons.get(i).setOnClickListener(v -> {
                changeButton();
            });
        }
    }

    public void changeButton() {
        initializeButtons();
        for (int i = 0; i <8; i++) {
            if (player1Turn) {
                XOButtons.get(i).setText("X");
                XOButtons.get(i).setClickable(false);
                player2Turn = true;
                player1Turn = false;
                changeTurnAlert(turnAlert);
            }
        }
    }



    public void changeTurnAlert(TextView alert) {
        if (player1Turn) {
            alert.setText("Player 1 turn");
        } else if (player2Turn) {
            alert.setText("Player 2 turn");
        }
    }

    public void checkForWinner() {


    }
}
package com.example.theme;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    SharedPreferences themeSettings;
    SharedPreferences.Editor settingsEditor;
    ImageButton imageTheme;

    private Button[] buttons;
    private int[][] winningCombinations = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
            {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
            {0, 4, 8}, {2, 4, 6}
    };
    private int currentPlayer = 0;
    private int winsX, winsO, draws;
    private SharedPreferences sharedPreferences;
    private boolean isBotGame = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        themeSettings = getSharedPreferences("SETTINGS", MODE_PRIVATE);
        settingsEditor = themeSettings.edit();

        if (!themeSettings.contains("MODE_NIGHT_ON")) {
            saveNightMode(false);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            Toast.makeText(this, "С первым запуском", Toast.LENGTH_SHORT).show();
        } else {
            setCurrentTheme();
        }

        setContentView(R.layout.activity_main);
        imageTheme = findViewById(R.id.imgBtn);
        updateImageButton();

        buttons = new Button[9];
        buttons[0] = findViewById(R.id.button0);
        buttons[1] = findViewById(R.id.button1);
        buttons[2] = findViewById(R.id.button2);
        buttons[3] = findViewById(R.id.button3);
        buttons[4] = findViewById(R.id.button4);
        buttons[5] = findViewById(R.id.button5);
        buttons[6] = findViewById(R.id.button6);
        buttons[7] = findViewById(R.id.button7);
        buttons[8] = findViewById(R.id.button8);

        sharedPreferences = getSharedPreferences("statistics", MODE_PRIVATE);
        winsX = sharedPreferences.getInt("winsX", 0);
        winsO = sharedPreferences.getInt("winsO", 0);
        draws = sharedPreferences.getInt("draws", 0);

        // Кнопки для выбора режима игры
        findViewById(R.id.playWithBotButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBotGame = true;
                resetGame();
            }
        });

        findViewById(R.id.playWithFriendButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBotGame = false;
                resetGame();
            }
        });

        for (Button button : buttons) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Button button = (Button) v;
                    if (button.getText().toString().isEmpty()) {
                        button.setText(currentPlayer == 0 ? "X" : "O");
                        if (checkWin()) {
                            if (currentPlayer == 0) {
                                winsX++;
                            } else {
                                winsO++;
                            }
                            updateStatistics();
                            resetGame();
                        } else if (checkDraw()) {
                            draws++;
                            updateStatistics();
                            resetGame();
                        } else {
                            currentPlayer = 1 - currentPlayer;
                            if (isBotGame && currentPlayer == 1) {
                                botMove(); 
                            }
                        }
                    }
                }
            });
        }

        findViewById(R.id.resetButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetGame();
            }
        });

        updateStatistics();

        imageTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isNightModeOn = themeSettings.getBoolean("MODE_NIGHT_ON", false);
                if (isNightModeOn) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    saveNightMode(false);
                    Toast.makeText(MainActivity.this, "Темная тема отключена", Toast.LENGTH_SHORT).show();
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    saveNightMode(true);
                    Toast.makeText(MainActivity.this, "Темная тема включена", Toast.LENGTH_SHORT).show();
                }
                updateImageButton();
            }
        });
    }

    private boolean checkWin() {
        for (int[] combination : winningCombinations) {
            if (buttons[combination[0]].getText().toString().equals(buttons[combination[1]].getText().toString()) &&
                    buttons[combination[1]].getText().toString().equals(buttons[combination[2]].getText().toString()) &&
                    !buttons[combination[0]].getText().toString().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean checkDraw() {
        for (Button button : buttons) {
            if (button.getText().toString().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void resetGame() {
        for (Button button : buttons) {
            button.setText("");
        }
        currentPlayer = 0;
    }

    private void updateStatistics() {
        TextView statistics = findViewById(R.id.statistics);
        statistics.setText("Победы X: " + winsX + ", Победы O: " + winsO + ", Ничьи: " + draws);
        sharedPreferences.edit()
                .putInt("winsX", winsX)
                .putInt("winsO", winsO)
                .putInt("draws", draws)
                .apply();
    }

    private void updateImageButton() {
        boolean isNightModeOn = themeSettings.getBoolean("MODE_NIGHT_ON", false);
        imageTheme.setImageResource(isNightModeOn ? R.drawable.knopkatem : R.drawable.knopka);
    }

    private void setCurrentTheme() {
        if (themeSettings.getBoolean("MODE_NIGHT_ON", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void saveNightMode(boolean isNightModeOn) {
        settingsEditor.putBoolean("MODE_NIGHT_ON", isNightModeOn);
        settingsEditor.apply();
    }

    private void botMove() {
        Random random = new Random();
        int index;
        do {
            index = random.nextInt(9);
        } while (!buttons[index].getText().toString().isEmpty());
        buttons[index].setText("O");
        if (checkWin()) {
            winsO++;
            updateStatistics();
            resetGame();
        } else if (checkDraw()) {
            draws++;
            updateStatistics();
            resetGame();
        } else {
            currentPlayer = 1 - currentPlayer;
        }
    }
}
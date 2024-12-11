package com.example.galacticat;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;


public class GameOver extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.value_screen);

        Objects.requireNonNull(getSupportActionBar()).hide();

        // Retrieve the time value passed from MainActivity
        long elapsedTime = getIntent().getLongExtra("elapsed_time", 0);

        TextView timeView = findViewById(R.id.time);
        timeView.setText("The cat was alive " + elapsedTime + " seconds");
    }

}

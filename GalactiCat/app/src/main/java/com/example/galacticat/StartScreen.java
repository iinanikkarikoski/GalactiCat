package com.example.galacticat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class StartScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_screen);

        Objects.requireNonNull(getSupportActionBar()).hide();

        navigation();
    }

    //Navigating to mainActivity after pressing the button
    private void navigation () {
        Button b1 = findViewById(R.id.start_btn);
        b1.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(StartScreen.this,MainActivity.class);
                        startActivity(i);
                    }
                }
        );
    }
}

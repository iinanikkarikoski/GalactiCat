package com.example.galacticat;

import android.os.Bundle;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;


public class ValueScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.value_screen);

        addTemp();
    }

    public void addTemp () {

        float temp = Data.getInstance().getTemperature();
        //updating the array so the list updates all the time etc.. not working
        while (temp > 0) {

        }

    }

}

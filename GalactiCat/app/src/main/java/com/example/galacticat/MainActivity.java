package com.example.galacticat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;

import java.util.ArrayList;


//README: xml tiedosto res>layout>activity_main.xml
// lol.kt on mainactivityn kotlin versio, ei tee mitää mutten uskaltanu poistaa sitä XD

public class MainActivity extends AppCompatActivity {

    private Mqtt5AsyncClient client;
    private float temperature;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        navigation();
    }

    //Navigation to the value screen
    private void navigation () {
        ImageButton b1 = findViewById(R.id.moreValues);
        b1.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(MainActivity.this,ValueScreen.class);
                        startActivity(i);
                    }
                }
        );
    }

    private void init () {
        Button btn = findViewById(R.id.btn_Connect);

        //Setting up the client
        client = MqttClient.builder()
                .useMqttVersion5()
                .serverHost("50257ce6934e4209a8fa688ee54347e8.s1.eu.hivemq.cloud")
                .serverPort(8883)
                .sslWithDefaultConfig()
                .simpleAuth()
                .username("kissa")
                .password("Kissa222".getBytes())
                .applySimpleAuth().buildAsync();

        btn.setOnClickListener(v -> Connect());
    }

    //Connects to the MQTT
    @SuppressLint("SetTextI18n")
    private void Connect () {

        client.connect().whenComplete((ack, throwable) -> {
            TextView messageTextView = findViewById(R.id.connection_info);

            if (throwable != null) {
                messageTextView.setText("Connection failed");
                Log.e("MQTT", "Connection failed", throwable);

            } else {
                messageTextView.setText("Connected successfully");
                Log.d("MQTT", "Connected successfully");

                //If successfully connected, goes to subscribe the topic
                Subscribe();
            }

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Clearing the text after 5 seconds
                    messageTextView.setText("");
                }
            }, 3000);
        });
    }

    //Subscribing the temperature values from MQTT
    @SuppressLint("SetTextI18n")
    private void Subscribe () {

        client.toAsync().subscribeWith()
                .topicFilter("kissa/temp")
                .callback(publish -> {
                    Log.d("MQTT", "Received message: " + new String(publish.getPayloadAsBytes()));

                    String receivedMessage = new String(publish.getPayloadAsBytes());
                    temperature = Float.parseFloat(receivedMessage);

                    //Writing the temp to the app
                    runOnUiThread(() -> {
                        TextView messageTextView = findViewById(R.id.tv_message);
                        messageTextView.setText("Current temperature: " + receivedMessage);

                        //Set temperature to the data class
                        Data.getInstance().setTemperature(temperature);

                        //Goes to check the temperature
                        CheckTemp(temperature);
                    });
                })
                .send();
    }

    //Changing the cat image according to the current temperature
    @SuppressLint("SetTextI18n")
    private void CheckTemp (float temp) {

        if (temp > 33) {  //dead
            ImageView image = findViewById(R.id.catimage);
            image.setImageResource(R.drawable.cat_burning);

            TextView text = findViewById(R.id.cat_info);
            text.setText("Oh no! The cat is burning hot!");
        }

        else if (temp > 30) {  //hot
            ImageView image = findViewById(R.id.catimage);
            image.setImageResource(R.drawable.cat_hot);

            TextView text = findViewById(R.id.cat_info);
            text.setText("Be careful! The cat is feeling hot!");
        }

        else if (temp > 28) {  //normal
            ImageView image = findViewById(R.id.catimage);
            image.setImageResource(R.drawable.cat_normal);

            TextView text = findViewById(R.id.cat_info);
            text.setText("Great! The cat is happy!");
        }

        else if (temp > 26) { //cold
            ImageView image = findViewById(R.id.catimage);
            image.setImageResource(R.drawable.cat_cold);

            TextView text = findViewById(R.id.cat_info);
            text.setText("Be careful! The cat is feeling cold!");
        }

        else if (temp > 25) {  //dead
            ImageView image = findViewById(R.id.catimage);
            image.setImageResource(R.drawable.cat_freezing);

            TextView text = findViewById(R.id.cat_info);
            text.setText("Oh no! The cat is freezing!");
        }
    }
}

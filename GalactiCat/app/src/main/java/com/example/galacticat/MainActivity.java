package com.example.galacticat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;

import java.util.concurrent.atomic.AtomicReference;

//README: xml tiedosto res>layout>activity_main.xml
// lol.kt on mainactivityn kotlin versio, ei tee mitää mutten uskaltanu poistaa sitä XD


public class MainActivity extends AppCompatActivity {

    private Mqtt5AsyncClient client;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        navigation();
    }

    //navigointi sivujen välillä, ei oo vielä nappuloita millä navigoida niin ei toimi
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

        //setuppaa sen hivemq clientin
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

    //varmaa pitäis laittaa nii et samasta nappulasta voi disconnectaa

    //Yhistää mqtt, logissa viesti onnistuuko
    @SuppressLint("SetTextI18n")
    private void Connect () {

        client.connect().whenComplete((ack, throwable) -> {
            if (throwable != null) {
                TextView messageTextView = findViewById(R.id.connection_info);
                messageTextView.setText("Connection failed");
                Log.e("MQTT", "Connection failed", throwable);

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Clear the text after the delay
                        messageTextView.setText("");
                    }
                }, 5000); // Delay in milliseconds

            } else {
                TextView messageTextView = findViewById(R.id.connection_info);
                messageTextView.setText("Connected successfully");
                Log.d("MQTT", "Connected successfully");
                Subscribe();

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Clear the text after the delay
                        messageTextView.setText("");
                    }
                }, 5000); // Delay in milliseconds
            }
        });
    }

    //Subscribee lämpötila topicin
    @SuppressLint("SetTextI18n")
    private void Subscribe () {

        client.toAsync().subscribeWith()
                .topicFilter("kissa/temp")
                .callback(publish -> {
                    Log.d("MQTT", "Received message: " + new String(publish.getPayloadAsBytes()));

                    String receivedMessage = new String(publish.getPayloadAsBytes());
                    float temperature = Float.parseFloat(receivedMessage);

                    //kirjottaa näytölle
                    runOnUiThread(() -> {
                        TextView messageTextView = findViewById(R.id.tv_message);
                        messageTextView.setText("Current temperature: " + receivedMessage);

                        CheckTemp(temperature);
                    });
                })
                .send();
    }

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

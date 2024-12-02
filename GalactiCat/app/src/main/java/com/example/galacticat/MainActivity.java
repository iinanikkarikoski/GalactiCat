package com.example.galacticat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AppComponentFactory;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;

//README: xml tiedosto res>layout>activity_main.xml
// lol.kt on mainactivityn kotlin versio, ei tee mitää mutten uskaltanu poistaa sitä XD


public class MainActivity extends AppCompatActivity {

    private Mqtt5AsyncClient client;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    //navigointi sivujen välillä, ei oo vielä nappuloita millä navigoida niin ei toimi
    /*private void navigation () {
        Button b1 = findViewById(R.id.page1);
        b1.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(MainActivity.this,value_screen.class);
                        startActivity(i);
                    }
                }
        );
    }*/

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
            } else {
                TextView messageTextView = findViewById(R.id.connection_info);
                messageTextView.setText("Connected successfully");
                Log.d("MQTT", "Connected successfully");
                Subscribe();
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

                    //kirjottaa näytölle
                    runOnUiThread(() -> {
                        TextView messageTextView = findViewById(R.id.tv_message);
                        messageTextView.setText("Received message: " + receivedMessage);
                    });
                })
                .send();
    }
}

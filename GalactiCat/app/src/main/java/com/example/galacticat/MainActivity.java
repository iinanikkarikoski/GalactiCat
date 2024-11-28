package com.example.galacticat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;

//README: xml tiedosto res>layout>activity_main.xml
// lol.kt on mainactivityn kotlin versio, ei tee mitää mutten uskaltanu poistaa sitä XD


public class MainActivity extends Activity {

    private Mqtt5AsyncClient client;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
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

    //Yhistää mqtt, logissa viesti onnistuuko
    private void Connect () {

        client.connect().whenComplete((ack, throwable) -> {
            if (throwable != null) {
                Log.e("MQTT", "Connection failed", throwable);
            } else {
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

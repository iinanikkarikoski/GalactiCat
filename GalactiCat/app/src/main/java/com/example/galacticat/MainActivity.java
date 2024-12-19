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
import java.time.Instant;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.appcompat.app.AppCompatActivity;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private Mqtt5AsyncClient client;
    private float temperature;
    private int counter_cold;
    private int counter_hot;

    private final Handler timerHandler = new Handler();
    private Runnable timerRunnable;
    private long startTime;
    private boolean isTimerRunning = false;
    public long time;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Objects.requireNonNull(getSupportActionBar()).hide();

        init();
        createFile();
    }

    //Timer for measuring how long the cat was kept alive
    private void startTimer() {
        startTime = System.currentTimeMillis();
        isTimerRunning = true;

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isTimerRunning) {
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    Log.d("TIMER", "Elapsed Time: " + elapsedTime / 1000 + " seconds");

                    timerHandler.postDelayed(this, 1000);
                }
            }
        };

        timerHandler.post(timerRunnable);
    }

    //Creating text files for the data values and time values
    //Time values were used in the evaluation
    //Data values stores the gotten temperaturevalues from the sensor
    private void createFile() {
        try {
            File directory = getFilesDir();
            File file = new File(directory, "DataValues.txt");
            File time = new File(directory, "timeValues_normal.txt");

            if (file.createNewFile()) {
                Log.d("WRITING", "File created: " + file.getAbsolutePath());
            } else {
                Log.d("WRITING", "File already exists:" + file.getAbsolutePath());
            }

            if (time.createNewFile()) {
                Log.d("WRITING", "File created: " + file.getAbsolutePath());
            } else {
                Log.d("WRITING", "File already exists:" + file.getAbsolutePath());
            }

        } catch (IOException e) {
            Log.d("WRITING", "An error occurred.");
        }
    }

    //Navigation to the game over screen and resetting the counters for a new game
    private void navigation () {
        Intent i = new Intent(MainActivity.this, GameOver.class);
        i.putExtra("elapsed_time", time);
        startActivity(i);

        counter_hot = 0;
        counter_cold = 0;
    }

    private void init () {
        Button btn = findViewById(R.id.btn_Connect);

        //Setting up the client
        client = MqttClient.builder()
                .useMqttVersion5()
                .serverHost("xx")
                .serverPort(8883)
                .sslWithDefaultConfig()
                .simpleAuth()
                .username("xx")
                .password("xx".getBytes())
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

                //If successfully connected, goes to subscribe the topic and starts the game timer
                startTimer();
                Subscribe();
            }

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Clearing the text
                    messageTextView.setText("");
                }
            }, 3000);
        });
    }

    //Subscribing the temperature values from MQTT
    @SuppressLint("SetTextI18n")
    private void Subscribe () {

        client.toAsync().subscribeWith()
                .topicFilter("id/temp")
                .callback(publish -> {
                    Log.d("MQTT", "Received message: " + new String(publish.getPayloadAsBytes()));

                    String receivedMessage = new String(publish.getPayloadAsBytes());
                    temperature = Float.parseFloat(receivedMessage);

                    //Writing the temp to the app
                    runOnUiThread(() -> {
                        TextView messageTextView = findViewById(R.id.tv_message);
                        messageTextView.setText("Current temperature: " + receivedMessage + " °C");

                        try {
                            //Writing the gotten value to the text file with date and time stamp
                            @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String timestamp = formatter.format(new Date());

                            File directory = getFilesDir();
                            File file = new File(directory, "DataValues.txt");

                            FileWriter myWriter = new FileWriter(file, true);
                            myWriter.write("Date: " + timestamp + " | Temperature: " + temperature + "\n");
                            myWriter.close();
                            Log.d("WRITING", "Successfully wrote to the file.");
                        } catch (IOException e) {
                            Log.d("WRITING", "An error occurred.");
                        }

                        //Goes to check the temperature
                        CheckTemp(temperature);
                    });
                })
                .send();
    }

    //Changing the cat image according to the current temperature
    @SuppressLint("SetTextI18n")
    private void CheckTemp (float temp) {

        if (temp > 29) {  //dead
            ImageView image = findViewById(R.id.catimage);
            image.setImageResource(R.drawable.cat_burning);

            TextView text = findViewById(R.id.cat_info);
            text.setText("Oh no! The cat is burning hot!");
            counter_hot++;

            //If the temperature is too hot for too long, game over
            if (counter_hot == 3) {
                Disconnect();
            }
        }

        else if (temp > 27) {  //hot
            ImageView image = findViewById(R.id.catimage);
            image.setImageResource(R.drawable.cat_hot);

            TextView text = findViewById(R.id.cat_info);
            text.setText("Be careful! The cat is feeling hot!");
        }

        else if (temp > 25) {  //normal
            ImageView image = findViewById(R.id.catimage);
            image.setImageResource(R.drawable.cat_normal);

            TextView text = findViewById(R.id.cat_info);
            text.setText("Great! The cat is happy!");
        }

        else if (temp > 23) { //cold
            ImageView image = findViewById(R.id.catimage);
            image.setImageResource(R.drawable.cat_cold);

            TextView text = findViewById(R.id.cat_info);
            text.setText("Be careful! The cat is feeling cold!");
        }

        else if (temp > 20 && temp < 23) {  //dead;
            ImageView image = findViewById(R.id.catimage);
            image.setImageResource(R.drawable.cat_freezing);

            TextView text = findViewById(R.id.cat_info);
            text.setText("Oh no! The cat is freezing!");
            counter_cold++;

            //If the temperature is too cold for too long, game over
            if (counter_cold == 3) {
                Disconnect();
            }
        }

        //For evaluation, getting the timestamp and writing it to the text file
        long time = Instant.now().toEpochMilli();
        try {
            File directory = getFilesDir();
            File file = new File(directory, "timeValues_normal.txt");

            FileWriter myWriter = new FileWriter(file, true);
            myWriter.write(time + "\n");
            myWriter.close();
            Log.d("WRITING", "Successfully wrote to the file.");
        } catch (IOException e) {
            Log.d("WRITING", "An error occurred.");
        }

    }

    //Disconnecting the MQTT when game was over
    private void Disconnect() {
        if (client != null) {
            client.disconnect()
                    .whenComplete((ack, throwable) -> {
                        if (throwable != null) {
                            Log.e("MQTT", "Error during disconnection", throwable);
                        } else {
                            Log.d("MQTT", "Disconnected successfully");
                            stopTimer();
                        }
                    });
        } else {
            Log.d("MQTT", "Client is not connected or client is null.");
        }
    }

    //Stopping timer after game was over and navigating to the game over screen
    private void stopTimer() {
        isTimerRunning = false;
        timerHandler.removeCallbacks(timerRunnable);  // Stop the timer
        long elapsedTime = System.currentTimeMillis() - startTime;
        time = elapsedTime / 1000;
        Log.d("TIMER", "Total Elapsed Time: " + elapsedTime / 1000 + " seconds");

        navigation();
    }
}

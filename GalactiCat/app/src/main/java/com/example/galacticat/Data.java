package com.example.galacticat;

public class Data {

    private static Data instance;
    private float temperature;

    private Data() {} // Private constructor

    public static Data getInstance() {
        if (instance == null) {
            instance = new Data();
        }
        return instance;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

}

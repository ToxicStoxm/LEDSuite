package com.toxicstoxm.LEDSuite;

public class Human implements Entity {
    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public String getName() {
        return "Human";
    }
    public String getMood() {
        return "Happy";
    }
}

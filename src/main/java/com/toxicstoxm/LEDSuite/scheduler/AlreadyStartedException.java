package com.toxicstoxm.LEDSuite.scheduler;

public class AlreadyStartedException extends RuntimeException {
    public AlreadyStartedException(String message) {
        super(message);
    }
}

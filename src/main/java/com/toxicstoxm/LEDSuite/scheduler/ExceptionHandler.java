package com.toxicstoxm.LEDSuite.scheduler;

public interface ExceptionHandler {
    boolean handleException(Task Task, Throwable t);
}

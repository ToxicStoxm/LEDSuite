package com.toxicstoxm.LEDSuite.logger;

public interface Logger {

    default void setDefaultLogArea(LogArea logArea) {
        throw new UnsupportedOperationException("This logger implementation does not support log areas!");
    }

    default LogArea getLogArea() {
        throw new UnsupportedOperationException("This logger implementation does not support log areas!");
    }

    default void fatal(String message) {
        log("[FATAL]:      " + message);
    }
    default void error(String message) {
        log("[ERROR]:      " + message);
    }
    default void warn(String message) {
        log("[WARN]:       " + message);
    }
    default void info(String message) {
        log("[INFO]:       " + message);
    }
    default void debug(String message) {
        log("[DEBUG]:      " + message);
    }
    default void stacktrace(String message) {
        log("[STACKTRACE]: " + message);
    }

    default void fatal(String message, LogArea area) {
        throw new UnsupportedOperationException("This logger implementation does not support log areas!");
    }
    default void error(String message, LogArea area) {
        throw new UnsupportedOperationException("This logger implementation does not support log areas!");
    }
    default void warn(String message, LogArea area) {
        throw new UnsupportedOperationException("This logger implementation does not support log areas!");
    }
    default void info(String message, LogArea area) {
        throw new UnsupportedOperationException("This logger implementation does not support log areas!");
    }
    default void debug(String message, LogArea area) {
        throw new UnsupportedOperationException("This logger implementation does not support log areas!");
    }
    default void stacktrace(String message, LogArea area) {
        throw new UnsupportedOperationException("This logger implementation does not support log areas!");
    }

    void log(String message);
}

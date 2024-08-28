package com.toxicstoxm.LEDSuite.logger;

import com.toxicstoxm.LEDSuite.logger.areas.LogArea;

public interface Logger {

    default void setDefaultLogArea(LogArea logArea) {
        unsupportedOperation();    }

    default LogArea getDefaultLogArea() {
        unsupportedOperation();
        return null;
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
    default void verbose(String message) {
        log("[VERBOSE]:    " + message);
    }
    default void stacktrace(String message) {
        log("[STACKTRACE]: " + message);
    }

    default void fatal(String message, LogArea area) {
        unsupportedOperation();    }
    default void error(String message, LogArea area) {
        unsupportedOperation();    }
    default void warn(String message, LogArea area) {
        unsupportedOperation();    }
    default void info(String message, LogArea area) {
        unsupportedOperation();    }
    default void debug(String message, LogArea area) {
        unsupportedOperation();    }
    default void verbose(String message, LogArea area) {
        unsupportedOperation();    }
    default void stacktrace(String message, LogArea area) {
        unsupportedOperation();
    }

    default void unsupportedOperation() {
        throw new UnsupportedOperationException("This logger implementation does not support log areas!");
    }

    void log(String message);
}

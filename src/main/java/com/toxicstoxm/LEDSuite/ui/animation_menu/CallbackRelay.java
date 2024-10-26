package com.toxicstoxm.LEDSuite.ui.animation_menu;

@FunctionalInterface
public interface CallbackRelay {
    void enqueueMessage(String message);
}

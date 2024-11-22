package com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog;

public interface AuthStatus {
    void setAuthenticating();
    void setAuthenticated(boolean authenticated, String username);
    default void setAuthenticated(boolean authenticated) {
        setAuthenticated(authenticated, null);
    }
}

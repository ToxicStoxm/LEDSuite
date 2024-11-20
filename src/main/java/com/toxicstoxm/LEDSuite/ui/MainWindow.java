package com.toxicstoxm.LEDSuite.ui;

import org.gnome.adw.AlertDialog;
import org.gnome.adw.ApplicationWindow;

public interface MainWindow {
    ApplicationWindow asApplicationWindow();
    void toggleSidebar();
    void displayAboutDialog();
    void displayStatusDialog();
    void displayPreferencesDialog();
    void displayShortcutsWindow();
    void uploadPageSelect();
    void settingsDialogApply();
    void settingsDialogApplyFail();
    void setServerConnected(boolean connected);
    void showAnimationListSpinner(boolean showSpinner);
    void displayFileCollisionDialog(AlertDialog.ResponseCallback callback, String message);
}

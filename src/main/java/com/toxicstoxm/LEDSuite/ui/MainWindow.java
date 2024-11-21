package com.toxicstoxm.LEDSuite.ui;

import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.status_reply.FileState;
import com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.status_reply.StatusReplyPacket;
import com.toxicstoxm.LEDSuite.ui.animation_menu.AnimationMenu;
import com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog.ServerState;
import com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog.SettingsDialogEndpoint;
import com.toxicstoxm.LEDSuite.ui.dialogs.status_dialog.StatusDialogEndpoint;
import org.gnome.adw.AlertDialog;
import org.gnome.adw.ApplicationWindow;
import org.gnome.adw.Window;
import org.gnome.gtk.Widget;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface MainWindow extends StatusDialogEndpoint, SettingsDialogEndpoint, UploadPageEndpoint {
    ApplicationWindow asApplicationWindow();
    void toggleSidebar();
    void displayAboutDialog();
    void displayStatusDialog();
    void displayPreferencesDialog();
    void displayShortcutsWindow();
    void uploadPageSelect();
    void settingsDialogApply();
    void setServerConnected(boolean connected);
    void showAnimationListSpinner(boolean showSpinner);
    void displayFileCollisionDialog(AlertDialog.ResponseCallback callback, String message);
    void setUploadProgress(double fraction);
    void uploadFinished();
    void displayAnimationMenu(AnimationMenu menu);
    void updateAnimations(@NotNull Collection<StatusReplyPacket.Animation> updatedAnimations);
    void setAnimationControlButtonsState(@NotNull FileState state);
    void changeMainContent(Widget widget);
    void setAnimationListSensitive(boolean sensitive);
}

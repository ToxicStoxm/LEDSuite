package com.x_tornado10.lccp.ui;

import com.x_tornado10.lccp.LCCP;
import org.gnome.adw.HeaderBar;
import org.gnome.adw.Window;
import org.gnome.gtk.*;

import static com.x_tornado10.lccp.ui.Window.getAttrSmall;

public class SettingsWindow extends Window {
    public SettingsWindow() {
        setTitle("LED-Cube Control Panel Settings");
        var box = Box.builder()
                .setOrientation(Orientation.VERTICAL)
                .build();

        var settingsBox = new Box(Orientation.VERTICAL, 10);
        var serverSettingsBox = new Box(Orientation.VERTICAL, 10);
        var settingsMainBox = new Box(Orientation.VERTICAL, 5);

        var serverSettingsLabel = Label.builder().setAttributes(getAttrSmall()).setLabel("Local Settings").build();
        var settingsLabel = Label.builder().setAttributes(getAttrSmall()).setLabel("LED-Cube Settings").build();
        var statusBox = getBox();

        settingsBox.setHalign(Align.CENTER);
        settingsBox.append(statusBox);
        settingsBox.append(settingsLabel);
        serverSettingsBox.append(serverSettingsLabel);

        settingsMainBox.append(serverSettingsBox);
        settingsMainBox.append(settingsBox);

        var headerBar = new HeaderBar();
        box.append(headerBar);
        box.append(settingsMainBox);

        this.setContent(box);
    }

    private Box getBox() {
        var statusBox = new Box(Orientation.HORIZONTAL, 10);
        var toggleStatusSwitch = new Switch();
        toggleStatusSwitch.onStateSet(b -> {
            LCCP.mainWindow.setVisible(b);
            return false;
        });
        var toggleStatusLabel = new Label("Display Status Banner");
        toggleStatusSwitch.setState(LCCP.mainWindow.isBannerVisible());
        toggleStatusSwitch.setActive(true);
        statusBox.append(toggleStatusLabel);
        statusBox.append(toggleStatusSwitch);
        return statusBox;
    }
}

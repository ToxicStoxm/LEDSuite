package com.x_tornado10.lccp.ui;

import org.gnome.adw.HeaderBar;
import org.gnome.adw.Window;
import org.gnome.gtk.Box;
import org.gnome.gtk.Orientation;

@Deprecated(since = "v0.0.1", forRemoval = true)
public class StatusWindow extends Window {
    public StatusWindow() {
        setTitle("LED-Cube Status");
        var box = Box.builder()
                .setOrientation(Orientation.VERTICAL)
                .build();
        var headerBar = new HeaderBar();
        box.append(headerBar);

        this.setContent(box);
    }
}

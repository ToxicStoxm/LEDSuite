package com.x_tornado10.lccp.ui;

import org.gnome.adw.PreferencesGroup;
import org.gnome.adw.PreferencesPage;

public class AnimationMenu extends PreferencesPage {
    public static AnimationMenu display(com.x_tornado10.lccp.yaml_factory.AnimationMenu animationMenu) {
        return new AnimationMenu().convert(animationMenu);
    }

    public AnimationMenu convert(com.x_tornado10.lccp.yaml_factory.AnimationMenu animationMenu) {
        this.setIconName("con.x_tornado10.lccp");
        this.setTitle("Animation menu!");
        this.add(PreferencesGroup.builder().setTitle("Animation menu group!").build());
        return this;
    }

    //public static AnimationMenu
}

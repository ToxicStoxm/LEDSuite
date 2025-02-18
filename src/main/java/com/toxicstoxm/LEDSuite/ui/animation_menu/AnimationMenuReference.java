package com.toxicstoxm.LEDSuite.ui.animation_menu;

import org.gnome.gtk.Image;

/**
 * Add interface to hold reference to an {@link AnimationMenu}'s update methods.
 * @since 1.0.0
 */
public interface AnimationMenuReference {
    void updateLabel(String label);
    void updateIcon(Image iconName);
}

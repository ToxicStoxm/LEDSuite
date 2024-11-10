package com.toxicstoxm.LEDSuite.ui;

import com.toxicstoxm.LEDSuite.time.Action;
import lombok.Builder;
import org.gnome.gtk.Application;
import org.jetbrains.annotations.NotNull;

/**
 * Wrapper record for an individual animation row.
 * Allows for quick and simple creating of new animation rows.
 * @since 1.0.0
 * @param app the main application instance
 * @param iconName the animation-icon-name
 * @param label the animation-name
 * @param animationID the animation-id or filename
 * @param action the animation-action-name, used for handling interactions
 * @param cooldown minimum time that must pass between interactions
 */
@Builder
public record AnimationRowData(@NotNull Application app, String iconName, String label, String animationID, Action action, Long cooldown) {}

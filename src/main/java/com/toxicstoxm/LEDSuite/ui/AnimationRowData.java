package com.toxicstoxm.LEDSuite.ui;

import com.toxicstoxm.LEDSuite.time.Action;
import lombok.Builder;
import org.gnome.gtk.Application;
import org.gnome.gtk.Image;
import org.jetbrains.annotations.NotNull;

/**
 * A data record representing an individual animation row.
 * This record is used to store all the necessary information for creating and managing an animation row in the UI.
 * It simplifies the creation process by grouping together key data like the application instance, icon, label, and action associated with the animation.
 * The data is primarily used by the {@link AnimationRow} class to create new rows in the sidebar.
 *
 * <p>This record provides a convenient way to pass and initialize all the required properties when constructing an animation row.</p>
 *
 * @since 1.0.0
 *
 * @param app The main {@link Application} instance for the GTK application. This is used to associate the row with the application.
 * @param icon The GTK name or base64 encoded image file of the icon to represent the animation.
 * @param label The name or description of the animation. This text is displayed on the animation row.
 * @param animationID The unique identifier for the animation. This could be the animation's ID or the filename associated with it.
 * @param action The action associated with this animation, typically used to handle interactions like button clicks. This is typically a unique action name used to trigger specific behavior.
 * @param cooldown The minimum time (in milliseconds) that must pass between interactions with the animation row. This is used to prevent repeated activation (e.g., avoiding accidental double-clicks).
 */
@Builder
public record AnimationRowData(
        @NotNull Application app,
        Image icon,
        String label,
        String animationID,
        Action action,
        Long cooldown,
        Long lastAccessed
) {}

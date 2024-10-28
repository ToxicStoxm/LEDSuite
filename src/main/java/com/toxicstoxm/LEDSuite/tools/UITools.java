package com.toxicstoxm.LEDSuite.tools;

import com.toxicstoxm.LEDSuite.Constants;
import org.gnome.adw.ActionRow;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class UITools {

    public static void markUnavailable(@NotNull ActionRow row) {
        row.setSubtitle(Constants.UI.NOT_AVAILABLE_VALUE);
        markUnavailableWithoutSubtitle(row);
        row.setSubtitleSelectable(false);
    }

    public static void markUnavailableWithoutSubtitle(@NotNull ActionRow row) {
        row.setOpacity(Constants.UI.REDUCED_OPACITY);
        row.setSelectable(false);
    }

    public static void markAllUnavailable(@NotNull Collection<ActionRow> rows) {
        rows.forEach(UITools::markUnavailable);
    }

    public static void markAllUnavailableWithoutSubtitle(@NotNull Collection<ActionRow> rows) {
        rows.forEach(UITools::markUnavailableWithoutSubtitle);
    }

    public static void markAvailable(@NotNull ActionRow row) {
        row.setOpacity(Constants.UI.DEFAULT_OPACITY);
        row.setSelectable(true);
        row.setSubtitleSelectable(true);
    }

    public static void markAllAvailable(@NotNull Collection<ActionRow> rows) {
        rows.forEach(UITools::markAvailable);
    }

}

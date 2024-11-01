package com.toxicstoxm.LEDSuite.ui;

import com.toxicstoxm.LEDSuite.time.Action;
import lombok.Builder;
import org.gnome.gtk.Application;
import org.jetbrains.annotations.NotNull;

@Builder
public record AnimationRowData(@NotNull Application app, String iconName, String label, String animationID, Action action, Long cooldown) {}

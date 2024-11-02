package com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu;

import com.toxicstoxm.YAJSI.api.yaml.ConfigurationSection;
import lombok.Builder;

@Builder
public record DeserializableWidget(ConfigurationSection widgetSection, String widgetKey) {}

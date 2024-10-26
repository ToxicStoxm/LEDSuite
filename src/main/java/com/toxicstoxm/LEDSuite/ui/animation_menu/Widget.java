package com.toxicstoxm.LEDSuite.ui.animation_menu;

import com.toxicstoxm.LEDSuite.communication.packet_management.PacketManager;
import com.toxicstoxm.YAJSI.api.file.YamlConfiguration;
import com.toxicstoxm.YAJSI.api.yaml.ConfigurationSection;

public interface Widget {

    String getType();

    YamlConfiguration serialize();

    Widget deserialize(ConfigurationSection yaml, String yamlPath) throws PacketManager.DeserializationException;

    org.gnome.gtk.Widget asAdwaitaWidget(CallbackRelay callbackRelay);
}

module com.toxicstoxm.LEDSuite {
    requires org.gnome.adw;
    requires org.yaml.snakeyaml;
    requires java.net.http;
    requires org.glassfish.tyrus.client;
    requires jakarta.websocket.client;
    requires YAJL;
    requires YAJSI;
    requires static lombok;
    requires org.jetbrains.annotations;
    requires java.desktop;
    requires io.github.classgraph;
    exports com.toxicstoxm.LEDSuite;
    exports com.toxicstoxm.LEDSuite.logger;
    exports com.toxicstoxm.LEDSuite.ui to org.gnome.glib,org.gnome.gobject,org.gnome.gdk,org.gnome.gtk;
    exports com.toxicstoxm.LEDSuite.ui.dialogs to org.gnome.glib,org.gnome.gobject,org.gnome.gdk,org.gnome.gtk;
    exports com.toxicstoxm.LEDSuite.settings;
    exports com.toxicstoxm.LEDSuite.communication.websocket;
    exports com.toxicstoxm.LEDSuite.communication.packet_management;
    exports com.toxicstoxm.LEDSuite.time;
    exports com.toxicstoxm.LEDSuite.ui.animation_menu to org.gnome.gdk, org.gnome.glib, org.gnome.gobject, org.gnome.gtk;
    exports com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog to org.gnome.gdk, org.gnome.glib, org.gnome.gobject, org.gnome.gtk;
    exports com.toxicstoxm.LEDSuite.ui.dialogs.status_dialog to org.gnome.gdk, org.gnome.glib, org.gnome.gobject, org.gnome.gtk;
    exports com.toxicstoxm.LEDSuite.communication.packet_management.packets.replys.status_reply;
    exports com.toxicstoxm.LEDSuite.auto_registration;
    exports com.toxicstoxm.LEDSuite.auto_registration.modules;
    exports com.toxicstoxm.LEDSuite.communication.packet_management.packets;
}
module com.toxicstoxm.LEDSuite {
    requires org.gnome.adw;
    requires org.yaml.snakeyaml;
    requires java.net.http;
    requires org.glassfish.tyrus.client;
    requires jakarta.websocket.client;
    requires java.logging;
    requires YAJL;
    requires YAJSI;
    requires static lombok;
    requires org.jetbrains.annotations;
    requires java.desktop;
    exports com.toxicstoxm.LEDSuite;
    exports com.toxicstoxm.LEDSuite.logger;
    exports com.toxicstoxm.LEDSuite.ui to org.gnome.glib,org.gnome.gobject,org.gnome.gdk,org.gnome.gtk;
    exports com.toxicstoxm.LEDSuite.ui.dialogs to org.gnome.glib,org.gnome.gobject,org.gnome.gdk,org.gnome.gtk;
    exports com.toxicstoxm.LEDSuite.settings;
    exports com.toxicstoxm.LEDSuite.time;
}
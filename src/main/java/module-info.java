module com.toxicstoxm.LEDSuite {
    requires org.gnome.adw;
    requires static lombok;
    requires org.jetbrains.annotations;
    requires org.yaml.snakeyaml;
    requires java.desktop;
    requires java.net.http;
    requires org.glassfish.tyrus.client;
    requires jakarta.websocket.client;
    requires java.logging;
    exports com.toxicstoxm.LEDSuite;
    exports com.toxicstoxm.LEDSuite.settings.config;
    exports com.toxicstoxm.LEDSuite.logger;
    exports com.toxicstoxm.LEDSuite.ui to org.gnome.glib,org.gnome.gobject,org.gnome.gdk,org.gnome.gtk;
    exports com.toxicstoxm.LEDSuite.logger.areas;
    exports com.toxicstoxm.LEDSuite.logger.levels;
}
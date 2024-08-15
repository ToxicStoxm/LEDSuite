module com.toxicstoxm.LEDSuite {
    requires org.gnome.adw;
    requires static lombok;
    requires org.jetbrains.annotations;
    requires java.desktop;
    exports com.toxicstoxm.LEDSuite;
    exports com.toxicstoxm.LEDSuite.ui to org.gnome.glib,org.gnome.gobject,org.gnome.gdk,org.gnome.gtk;
}
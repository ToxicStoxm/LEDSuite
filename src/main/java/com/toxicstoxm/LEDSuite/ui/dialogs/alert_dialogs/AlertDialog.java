package com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs;

import com.toxicstoxm.LEDSuite.auto_registration.AutoRegistrableItem;
import org.gnome.gtk.Widget;

public interface AlertDialog<T> extends AutoRegistrableItem {

    AlertDialog<T> create(T data);

    void present(Widget parent);

}

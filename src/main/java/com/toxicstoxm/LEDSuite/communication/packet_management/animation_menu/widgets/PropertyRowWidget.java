package com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.widgets;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.auto_registration.AutoRegister;
import com.toxicstoxm.LEDSuite.auto_registration.modules.AutoRegisterModules;
import com.toxicstoxm.LEDSuite.communication.packet_management.DeserializationException;
import com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.AnimationMenuWidget;
import com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.DeserializableWidget;
import com.toxicstoxm.LEDSuite.communication.packet_management.animation_menu.WidgetType;
import com.toxicstoxm.YAJSI.api.yaml.ConfigurationSection;
import org.gnome.adw.ActionRow;

@AutoRegister(module = AutoRegisterModules.WIDGETS)
public class PropertyRowWidget extends AnimationMenuWidget {

    @Override
    public String getType() {
        return WidgetType.PROPERTY_ROW.getName();
    }

    @Override
    public org.gnome.gtk.Widget deserialize(DeserializableWidget deserializableWidget) throws DeserializationException {
        super.deserialize(deserializableWidget);
        ActionRow widget = ActionRow.builder().setTooltipText(toolTip).build();
        widget.setCssClasses(new String[]{"property"});
        ConfigurationSection widgetSection = deserializableWidget.widgetSection();

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.LABEL, widgetSection)) {
            widget.setTitle(widgetSection.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.LABEL));
        }

        if (checkIfKeyExists(Constants.Communication.YAML.Keys.Reply.MenuReply.SUBTITLE, widgetSection)) {
            widget.setSubtitle(widgetSection.getString(Constants.Communication.YAML.Keys.Reply.MenuReply.SUBTITLE));
        }

        return widget;
    }
}

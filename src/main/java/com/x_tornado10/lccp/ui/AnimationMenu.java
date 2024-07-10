package com.x_tornado10.lccp.ui;

import com.x_tornado10.lccp.LCCP;
import com.x_tornado10.lccp.communication.network.Networking;
import com.x_tornado10.lccp.task_scheduler.LCCPRunnable;
import com.x_tornado10.lccp.yaml_factory.YAMLMessage;
import com.x_tornado10.lccp.yaml_factory.YAMLSerializer;
import com.x_tornado10.lccp.yaml_factory.wrappers.menu_wrappers.Container;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.gnome.adw.*;
import org.gnome.gtk.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AnimationMenu extends PreferencesPage {
    public static AnimationMenu display(com.x_tornado10.lccp.yaml_factory.AnimationMenu animationMenu) {
        return new AnimationMenu().convert(animationMenu);
    }

    public AnimationMenu convert(com.x_tornado10.lccp.yaml_factory.AnimationMenu animationMenu) {

        this.setIconName(animationMenu.getIcon());
        this.setTitle(animationMenu.getLabel());

        for (Map.Entry<Integer, com.x_tornado10.lccp.yaml_factory.AnimationMenu.WidgetMain> entry: animationMenu.getContent().entrySet()) {
            com.x_tornado10.lccp.yaml_factory.AnimationMenu.WidgetMain val = entry.getValue();
            if (val instanceof Container && val.getType() == com.x_tornado10.lccp.yaml_factory.AnimationMenu.WidgetType.group) {
               insertGroup((com.x_tornado10.lccp.yaml_factory.AnimationMenu.AnimationMenuGroup) val);
            }
        }
        return this;
    }

    public void insertSdt(Widget widget, com.x_tornado10.lccp.yaml_factory.AnimationMenu.WidgetMain widgetMain) {
        widget.setTooltipText(widgetMain.getTooltip());
        widget.setCssClasses(styleStringToCSSArray(widgetMain.getStyle()));
        widget.setName(widgetMain.getLabel());
    }
    public void insertSdtWithoutStyle(Widget widget, com.x_tornado10.lccp.yaml_factory.AnimationMenu.WidgetMain widgetMain) {
        widget.setTooltipText(widgetMain.getTooltip());
        widget.setName(widgetMain.getLabel());
    }

    public void insertChildren(PreferencesGroup prefGroup, com.x_tornado10.lccp.yaml_factory.AnimationMenu.AnimationMenuGroup menuGroup) {
        for (Map.Entry<Integer, com.x_tornado10.lccp.yaml_factory.AnimationMenu.WidgetMain> entry : menuGroup.getContent().entrySet()) {
            com.x_tornado10.lccp.yaml_factory.AnimationMenu.WidgetMain val = entry.getValue();

            switch (val.getType()) {
                //case group -> insertGroup((com.x_tornado10.lccp.yaml_factory.AnimationMenu.AnimationMenuGroup) val, );
                case expander -> insertExpander((com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Expander) val, prefGroup);
                case button -> insertButton((com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Button) val, prefGroup);
                case entry -> insertEntry((com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Entry) val, prefGroup);
                case slider -> insertSlider((com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Slider) val, prefGroup);
                case _switch -> insertSwitch((com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Switch) val, prefGroup);
                case spinner -> insertSpinner((com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Spinner) val, prefGroup);
                case dropdown -> insertDropdown((com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Dropdown) val, prefGroup);
                case property -> insertProperty((com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Property) val, prefGroup);
            }
        }
    }

    public void insertChildren(ExpanderRow expander, com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Expander _expander) {
        for (Map.Entry<Integer, com.x_tornado10.lccp.yaml_factory.AnimationMenu.WidgetMain> entry : _expander.getContent().entrySet()) {
            com.x_tornado10.lccp.yaml_factory.AnimationMenu.WidgetMain val = entry.getValue();
            switch (val.getType()) {
                case button -> insertButton((com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Button) val, expander);
                case entry -> insertEntry((com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Entry) val, expander);
                case slider -> insertSlider((com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Slider) val, expander);
                case _switch -> insertSwitch((com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Switch) val, expander);
                case spinner -> insertSpinner((com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Spinner) val, expander);
                case dropdown -> insertDropdown((com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Dropdown) val, expander);
                case property -> insertProperty((com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Property) val, expander);
            }

        }
    }

    public void insertGroup(com.x_tornado10.lccp.yaml_factory.AnimationMenu.AnimationMenuGroup group) {
        var pg = PreferencesGroup.builder()
                .setTitle(group.getLabel())
                .build();
        insertSdtWithoutStyle(pg, group);
        insertChildren(pg, group);
        this.add(pg);
    }

    public void insertExpander(com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Expander expander, PreferencesGroup prefGroup) {
        var expanderRow = ExpanderRow.builder()
                .setShowEnableSwitch(expander.isToggleable())
                .setEnableExpansion(expander.isValue())
                .setTitle(expander.getLabel())
                .build();
        insertSdtWithoutStyle(expanderRow, expander);
        insertChildren(expanderRow, expander);
        attachHandle(expanderRow, expander);
        prefGroup.add(expanderRow);
    }

    private void attachHandle(ExpanderRow expander, com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Expander _expander) {
        boolean[] temp = new boolean[]{expander.getEnableExpansion()};
        expander.onStateFlagsChanged(_ -> {
           if (temp[0] != expander.getEnableExpansion()) {
               temp[0] = expander.getEnableExpansion();
               try {
                   Networking.Communication.sendYAMLDefaultHost(
                           YAMLMessage.builder()
                                   .setPacketType(YAMLMessage.PACKET_TYPE.request)
                                   .setRequestType(YAMLMessage.REQUEST_TYPE.menu_change)
                                   .setObjectPath(_expander.getPath())
                                   .setObjectNewValue(String.valueOf(temp[0]))
                                   .build()
                   );
               } catch (ConfigurationException | YAMLSerializer.InvalidReplyTypeException |
                        YAMLSerializer.InvalidPacketTypeException | YAMLSerializer.TODOException e) {
                   throw new RuntimeException(e);
               }
           }
        });
    }

    public Widget getButton(com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Button button) {
        if (button.isRow()) {
            var _button = Button.builder()
                    .setIconName(button.getIcon())
                    .setLabel(button.getLabel())
                    .build();
            insertSdt(_button, button);
            attachHandle(_button, button);
            var buttonRow = ActionRow.builder()
                    .setTitle(button.getLabel())
                    .build();
            insertSdt(buttonRow, button);
            var clamp = Clamp.builder().setMaximumSize(40).setOrientation(Orientation.VERTICAL).setTighteningThreshold(40).build();
            clamp.setChild(_button);
            buttonRow.addSuffix(clamp);
            return buttonRow;
        } else {
            var _button = Button.builder()
                    .build();
            insertSdt(_button, button);
            attachHandle(_button, button);

            var buttonBox = Box.builder().setOrientation(Orientation.HORIZONTAL).setSpacing(5).build();
            buttonBox.append(Label.builder().setLabel(button.getLabel()).build());
            var clamp0 = Clamp.builder()
                    .setMaximumSize(70)
                    .setTighteningThreshold(70)
                    .setOrientation(Orientation.HORIZONTAL)
                    .setChild(buttonBox)
                    .build();

            _button.setChild(clamp0);

            return Clamp.builder()
                    .setMarginTop(25)
                    .setMaximumSize(120)
                    .setTighteningThreshold(120)
                    .setOrientation(Orientation.HORIZONTAL)
                    .setChild(_button)
                    .build();
        }
    }

    private void attachHandle(Button button, com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Button _button) {
        button.onClicked(() -> {
            try {
                Networking.Communication.sendYAMLDefaultHost(
                        YAMLMessage.builder()
                                .setPacketType(YAMLMessage.PACKET_TYPE.request)
                                .setRequestType(YAMLMessage.REQUEST_TYPE.menu_change)
                                .setObjectPath(_button.getPath())
                                .setObjectNewValue("")
                                .build()
                );
            } catch (ConfigurationException | YAMLSerializer.InvalidReplyTypeException |
                     YAMLSerializer.InvalidPacketTypeException | YAMLSerializer.TODOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void insertButton(com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Button button, ExpanderRow expander) {
        expander.addRow(getButton(button));
    }

    public void insertButton(com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Button button, PreferencesGroup prefGroup) {
        prefGroup.add(getButton(button));
    }

    public EntryRow getEntry(com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Entry entry) {
        var _entry = EntryRow.builder()
                .setTitle(entry.getLabel())
                .setText(entry.getContent())
                .setShowApplyButton(entry.isApplyButton())
                .setEditable(entry.isEditable())
                .build();
        insertSdtWithoutStyle(_entry, entry);
        attachHandle(_entry, entry);
        return _entry;
    }

    public void attachHandle(EntryRow entry, com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Entry _entry) {
        String[] temp = new String[]{entry.getText()};
        entry.onApply(() -> {
            if (!Objects.equals(temp[0], entry.getText())) {
                temp[0] = entry.getText();
                try {
                    Networking.Communication.sendYAMLDefaultHost(
                            YAMLMessage.builder()
                                    .setPacketType(YAMLMessage.PACKET_TYPE.request)
                                    .setRequestType(YAMLMessage.REQUEST_TYPE.menu_change)
                                    .setObjectPath(_entry.getPath())
                                    .setObjectNewValue(temp[0])
                                    .build()
                    );
                } catch (ConfigurationException | YAMLSerializer.InvalidReplyTypeException |
                         YAMLSerializer.InvalidPacketTypeException | YAMLSerializer.TODOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void insertEntry(com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Entry entry, ExpanderRow expander) {
        expander.addRow(getEntry(entry));
    }

    public void insertEntry(com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Entry entry, PreferencesGroup prefGroup) {
        prefGroup.add(getEntry(entry));
    }

    public SpinRow getSlider(com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Slider slider) {
        var _slider = SpinRow.builder()
                .setTitle(slider.getLabel())
                .setNumeric(slider.isNumeric())
                .setSnapToTicks(slider.isSnap())
                .setWrap(slider.isWraparound())
                .setDigits(slider.getDigits())
                .setClimbRate(slider.getClimb_rate())
                .setValue(slider.getValue())
                .build();
        _slider.setRange(slider.getMin(), slider.getMax());
        insertSdtWithoutStyle(_slider, slider);
        attachHandle(_slider, slider);
        return _slider;
    }

    private void attachHandle(SpinRow slider, com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Slider _slider) {
        slider.onChanged(() -> {
            try {
                Networking.Communication.sendYAMLDefaultHost(
                        YAMLMessage.builder()
                                .setPacketType(YAMLMessage.PACKET_TYPE.request)
                                .setRequestType(YAMLMessage.REQUEST_TYPE.menu_change)
                                .setObjectPath(_slider.getPath())
                                .setObjectNewValue(String.valueOf(slider.getValue()))
                                .build()
                );
            } catch (ConfigurationException | YAMLSerializer.InvalidReplyTypeException |
                     YAMLSerializer.InvalidPacketTypeException | YAMLSerializer.TODOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void insertSlider(com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Slider slider, ExpanderRow expander) {
        expander.addRow(getSlider(slider));
    }

    public void insertSlider(com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Slider slider, PreferencesGroup prefGroup) {
        prefGroup.add(getSlider(slider));
    }

    public SwitchRow getSwitch(com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Switch _switch) {
        var __switch = SwitchRow.builder()
                .setTitle(_switch.getLabel())
                .setActive(_switch.isValue())
                .build();
        insertSdtWithoutStyle(__switch, _switch);
        attachHandle(__switch, _switch);
        return __switch;
    }

    private void attachHandle(SwitchRow _switch, com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Switch __switch) {
        boolean[] temp = new boolean[]{_switch.getActive()};
        _switch.getActivatableWidget().onStateFlagsChanged(_ -> {
            boolean active = __switch.isValue();
            if (!temp[0] == active) {
                temp[0] = active;
                try {
                    Networking.Communication.sendYAMLDefaultHost(
                            YAMLMessage.builder()
                                    .setPacketType(YAMLMessage.PACKET_TYPE.request)
                                    .setRequestType(YAMLMessage.REQUEST_TYPE.menu_change)
                                    .setObjectPath(__switch.getPath())
                                    .setObjectNewValue(String.valueOf(active))
                                    .build()
                    );
                } catch (ConfigurationException | YAMLSerializer.InvalidReplyTypeException |
                         YAMLSerializer.InvalidPacketTypeException | YAMLSerializer.TODOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void insertSwitch(com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Switch _switch, ExpanderRow expander) {
        expander.addRow(getSwitch(_switch));
    }

    public void insertSwitch(com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Switch _switch, PreferencesGroup prefGroup) {
        prefGroup.add(getSwitch(_switch));
    }

    public Widget getSpinner(com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Spinner spinner) {
        var _spinner = Spinner.builder()
                .setSpinning(true)
                .build();
        return Clamp.builder()
                .setMarginTop(25)
                .setMaximumSize(120)
                .setTighteningThreshold(120)
                .setOrientation(Orientation.HORIZONTAL)
                .setChild(_spinner)
                .build();
    }

    public void insertSpinner(com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Spinner spinner, ExpanderRow expander) {
        Widget _spinner = getSpinner(spinner);
        expander.addRow(_spinner);
        new LCCPRunnable() {
            @Override
            public void run() {
                expander.remove(_spinner);
            }
        }.runTaskLaterAsynchronously((long) (spinner.getTime() * 1000));
    }

    public void insertSpinner(com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Spinner spinner, PreferencesGroup prefGroup) {
        Widget _spinner = getSpinner(spinner);
        prefGroup.add(_spinner);
        new LCCPRunnable() {
            @Override
            public void run() {
                prefGroup.remove(_spinner);
            }
        }.runTaskLaterAsynchronously((long) (spinner.getTime() * 1000));
    }

    public ComboRow getDropdown(com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Dropdown dropdown) {
        var _dropdown = ComboRow.builder()
                .setTitle(dropdown.getLabel())
                .setSubtitle(dropdown.getContent())
                .setEnableSearch(dropdown.isSearchable())
                .setModel(StringList.builder().setStrings(dropdown.getDropdown().toArray(new String[0])).build())
                .setSelected(dropdown.getSelected())
                .build();
        insertSdtWithoutStyle(_dropdown, dropdown);
        attachHandle(_dropdown, dropdown);
        return _dropdown;
    }

    private void attachHandle(ComboRow dropdown, com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Dropdown _dropdown) {
        int[] pos = new int[]{_dropdown.getSelected()};
        dropdown.onStateFlagsChanged(_ -> {
            if (dropdown.getSelected() != pos[0]) {
                pos[0] = dropdown.getSelected();
                try {
                    Networking.Communication.sendYAMLDefaultHost(
                            YAMLMessage.builder()
                                    .setPacketType(YAMLMessage.PACKET_TYPE.request)
                                    .setRequestType(YAMLMessage.REQUEST_TYPE.menu_change)
                                    .setObjectPath(_dropdown.getPath())
                                    .setObjectNewValue(_dropdown.getByIndex(pos[0]))
                                    .build()
                    );
                } catch (ConfigurationException | YAMLSerializer.InvalidReplyTypeException |
                         YAMLSerializer.InvalidPacketTypeException | YAMLSerializer.TODOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void insertDropdown(com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Dropdown dropdown, ExpanderRow expander) {
        expander.addRow(getDropdown(dropdown));
    }

    public void insertDropdown(com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Dropdown dropdown, PreferencesGroup prefGroup) {
        prefGroup.add(getDropdown(dropdown));
    }

    public ActionRow getProperty(com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Property property) {
        var _property = ActionRow.builder()
                .setTitle(property.getLabel())
                .setSubtitle(property.getContent())
                .setCssClasses(new String[]{"property"})
                .build();
        insertSdt(_property, property);
        return _property;
    }

    public void insertProperty(com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Property property, ExpanderRow expander) {
        expander.addRow(getProperty(property));
    }

    public void insertProperty(com.x_tornado10.lccp.yaml_factory.AnimationMenu.Widgets.Property property, PreferencesGroup prefGroup) {
        prefGroup.add(getProperty(property));
    }

    public static String[] styleStringToCSSArray(String styleString) {
        return styleStringToCSSArray(styleString, ",");
    }

    public static String[] styleStringToCSSArray(String styleString, String regex) {
        String[] result = styleString.split(regex);
        List<String> _result = new java.util.ArrayList<>(Arrays.stream(result).toList());
        _result.removeIf(s -> s == null || s.isEmpty() || s.isBlank());
        return _result.toArray(new String[0]);
    }
}

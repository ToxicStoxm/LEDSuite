package com.toxicstoxm.LEDSuite.ui;

import com.toxicstoxm.LEDSuite.communication.network.Networking;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteRunnable;
import com.toxicstoxm.LEDSuite.yaml_factory.YAMLMessage;
import com.toxicstoxm.LEDSuite.yaml_factory.YAMLSerializer;
import com.toxicstoxm.LEDSuite.yaml_factory.wrappers.menu_wrappers.Container;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.gnome.adw.*;
import org.gnome.gtk.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class AnimationMenu extends PreferencesPage {
    private Box contentBox;

    public static StatusPage display(com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu animationMenu) {
        return new AnimationMenu().convert(animationMenu);
    }

    public StatusPage convert(com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu animationMenu) {
        contentBox = Box.builder()
                .setOrientation(Orientation.VERTICAL)
                .setSpacing(12)
                .setHexpand(true)
                .build();
        this.setTitle(animationMenu.getLabel());

        for (Map.Entry<Integer, com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.LEDSuiteWidget> entry: animationMenu.getContent().entrySet()) {
            com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.LEDSuiteWidget val = entry.getValue();
            if (val instanceof Container && val.getType() == com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.WidgetType.group) {
                insertGroup((com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.AnimationMenuGroup) val);
            }
        }

        var contentWrapper = Clamp.builder()
                .setOrientation(Orientation.HORIZONTAL)
                .setChild(contentBox)
                .setMaximumSize(500)
                .setTighteningThreshold(500)
                .build();


        return StatusPage.builder()
                .setIconName(animationMenu.getIcon())
                .setTitle(animationMenu.getLabel())
                .setChild(contentWrapper)
                .build();
    }

    public void insertSdt(Widget widget, com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.LEDSuiteWidget LEDSuiteWidget) {
        widget.setTooltipText(LEDSuiteWidget.getTooltip());
        widget.setCssClasses(styleStringToCSSArray(LEDSuiteWidget.getStyle()));
        widget.setName(LEDSuiteWidget.getLabel());
    }
    public void insertSdtWithoutStyle(Widget widget, com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.LEDSuiteWidget LEDSuiteWidget) {
        widget.setTooltipText(LEDSuiteWidget.getTooltip());
        widget.setName(LEDSuiteWidget.getLabel());
    }

    public void insertChildren(PreferencesGroup prefGroup, com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.AnimationMenuGroup menuGroup) {
        for (Map.Entry<Integer, com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.LEDSuiteWidget> entry : menuGroup.getContent().entrySet()) {
            com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.LEDSuiteWidget val = entry.getValue();

            switch (val.getType()) {
                //case group -> insertGroup((com.x_tornado10.LEDSuite.yaml_factory.AnimationMenu.AnimationMenuGroup) val, );
                case expander -> insertExpander((com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Expander) val, prefGroup);
                case button -> insertButton((com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Button) val, prefGroup);
                case entry -> insertEntry((com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Entry) val, prefGroup);
                case slider -> insertSlider((com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Slider) val, prefGroup);
                case _switch -> insertSwitch((com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Switch) val, prefGroup);
                case spinner -> insertSpinner((com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Spinner) val, prefGroup);
                case dropdown -> insertDropdown((com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Dropdown) val, prefGroup);
                case property -> insertProperty((com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Property) val, prefGroup);
            }
        }
    }

    public void insertChildren(ExpanderRow expander, com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Expander _expander) {
        for (Map.Entry<Integer, com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.LEDSuiteWidget> entry : _expander.getContent().entrySet()) {
            com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.LEDSuiteWidget val = entry.getValue();
            switch (val.getType()) {
                case button -> insertButton((com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Button) val, expander);
                case entry -> insertEntry((com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Entry) val, expander);
                case slider -> insertSlider((com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Slider) val, expander);
                case _switch -> insertSwitch((com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Switch) val, expander);
                case spinner -> insertSpinner((com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Spinner) val, expander);
                case dropdown -> insertDropdown((com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Dropdown) val, expander);
                case property -> insertProperty((com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Property) val, expander);
            }

        }
    }

    public void insertGroup(com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.AnimationMenuGroup group) {
        var pg = PreferencesGroup.builder()
                .setTitle(group.getLabel())
                .build();
        insertSdtWithoutStyle(pg, group);
        insertChildren(pg, group);
        contentBox.append(pg);
    }

    public void insertExpander(com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Expander expander, PreferencesGroup prefGroup) {
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

    private void attachHandle(ExpanderRow expander, com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Expander _expander) {
        AtomicBoolean temp = new AtomicBoolean(expander.getEnableExpansion());
        expander.onStateFlagsChanged(_ -> {
            if (temp.get() != expander.getEnableExpansion()) {
                temp.set(expander.getEnableExpansion());
                try {
                    Networking.Communication.sendYAMLDefaultHost(
                            YAMLMessage.builder()
                                    .setPacketType(YAMLMessage.PACKET_TYPE.request)
                                    .setRequestType(YAMLMessage.REQUEST_TYPE.menu_change)
                                    .setRequestFile(this.getTitle())
                                    .setObjectPath(_expander.getPath())
                                    .setObjectNewValue(String.valueOf(temp.get()))
                                    .build()
                    );
                } catch (ConfigurationException | YAMLSerializer.InvalidReplyTypeException |
                         YAMLSerializer.InvalidPacketTypeException | YAMLSerializer.TODOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public Widget getButton(com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Button button) {
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

    private void attachHandle(Button button, com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Button _button) {
        button.onClicked(() -> {
            try {
                Networking.Communication.sendYAMLDefaultHost(
                        YAMLMessage.builder()
                                .setPacketType(YAMLMessage.PACKET_TYPE.request)
                                .setRequestType(YAMLMessage.REQUEST_TYPE.menu_change)
                                .setRequestFile(this.getTitle())
                                .setObjectPath(_button.getPath())
                                .setObjectNewValue("click")
                                .build()
                );
            } catch (ConfigurationException | YAMLSerializer.InvalidReplyTypeException |
                     YAMLSerializer.InvalidPacketTypeException | YAMLSerializer.TODOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void insertButton(com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Button button, ExpanderRow expander) {
        expander.addRow(getButton(button));
    }

    public void insertButton(com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Button button, PreferencesGroup prefGroup) {
        prefGroup.add(getButton(button));
    }

    public EntryRow getEntry(com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Entry entry) {
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

    public void attachHandle(EntryRow entry, com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Entry _entry) {
        AtomicReference<String> temp = new AtomicReference<>(entry.getText());
        entry.onApply(() -> {
            if (!Objects.equals(temp.get(), entry.getText())) {
                temp.set(entry.getText());
                try {
                    Networking.Communication.sendYAMLDefaultHost(
                            YAMLMessage.builder()
                                    .setPacketType(YAMLMessage.PACKET_TYPE.request)
                                    .setRequestType(YAMLMessage.REQUEST_TYPE.menu_change)
                                    .setRequestFile(this.getTitle())
                                    .setObjectPath(_entry.getPath())
                                    .setObjectNewValue(temp.get())
                                    .build()
                    );
                } catch (ConfigurationException | YAMLSerializer.InvalidReplyTypeException |
                         YAMLSerializer.InvalidPacketTypeException | YAMLSerializer.TODOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void insertEntry(com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Entry entry, ExpanderRow expander) {
        expander.addRow(getEntry(entry));
    }

    public void insertEntry(com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Entry entry, PreferencesGroup prefGroup) {
        prefGroup.add(getEntry(entry));
    }

    public SpinRow getSlider(com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Slider slider) {
        var _slider = SpinRow.withRange(slider.getMin(), slider.getMax(), slider.getStep());
        _slider.setTitle(slider.getLabel());
        _slider.setNumeric(slider.isNumeric());
        _slider.setSnapToTicks(slider.isSnap());
        _slider.setWrap(slider.isWraparound());
        _slider.setDigits(slider.getDigits());
        _slider.setClimbRate(slider.getClimb_rate());
        _slider.setValue(slider.getValue());
        _slider.setUpdatePolicy(SpinButtonUpdatePolicy.IF_VALID);

        insertSdtWithoutStyle(_slider, slider);
        attachHandle(_slider, slider);
        return _slider;
    }

    private void attachHandle(SpinRow slider, com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Slider _slider) {
        slider.onChanged(() -> {
            try {
                Networking.Communication.sendYAMLDefaultHost(
                        YAMLMessage.builder()
                                .setPacketType(YAMLMessage.PACKET_TYPE.request)
                                .setRequestType(YAMLMessage.REQUEST_TYPE.menu_change)
                                .setRequestFile(this.getTitle())
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

    public void insertSlider(com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Slider slider, ExpanderRow expander) {
        expander.addRow(getSlider(slider));
    }

    public void insertSlider(com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Slider slider, PreferencesGroup prefGroup) {
        prefGroup.add(getSlider(slider));
    }

    public SwitchRow getSwitch(com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Switch _switch) {
        var __switch = SwitchRow.builder()
                .setTitle(_switch.getLabel())
                .setActive(_switch.isValue())
                .build();
        insertSdtWithoutStyle(__switch, _switch);
        attachHandle(__switch, _switch);
        return __switch;
    }

    private void attachHandle(SwitchRow _switch, com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Switch __switch) {
        AtomicBoolean temp = new AtomicBoolean(_switch.getActive());
        _switch.getActivatableWidget().onStateFlagsChanged(_ -> {
            boolean active = _switch.getActive();
            if (temp.get() != active) {
                temp.set(active);
                try {
                    Networking.Communication.sendYAMLDefaultHost(
                            YAMLMessage.builder()
                                    .setPacketType(YAMLMessage.PACKET_TYPE.request)
                                    .setRequestType(YAMLMessage.REQUEST_TYPE.menu_change)
                                    .setRequestFile(this.getTitle())
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

    public void insertSwitch(com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Switch _switch, ExpanderRow expander) {
        expander.addRow(getSwitch(_switch));
    }

    public void insertSwitch(com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Switch _switch, PreferencesGroup prefGroup) {
        prefGroup.add(getSwitch(_switch));
    }

    public Widget getSpinner(com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Spinner spinner) {
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

    public void insertSpinner(com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Spinner spinner, ExpanderRow expander) {
        Widget _spinner = getSpinner(spinner);
        expander.addRow(_spinner);
        new LEDSuiteRunnable() {
            @Override
            public void run() {
                expander.remove(_spinner);
            }
        }.runTaskLaterAsynchronously((long) (spinner.getTime() * 1000));
    }

    public void insertSpinner(com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Spinner spinner, PreferencesGroup prefGroup) {
        Widget _spinner = getSpinner(spinner);
        prefGroup.add(_spinner);
        new LEDSuiteRunnable() {
            @Override
            public void run() {
                prefGroup.remove(_spinner);
            }
        }.runTaskLaterAsynchronously((long) (spinner.getTime() * 1000));
    }

    public ComboRow getDropdown(com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Dropdown dropdown) {
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

    private void attachHandle(ComboRow dropdown, com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Dropdown _dropdown) {
        AtomicInteger pos = new AtomicInteger(_dropdown.getSelected());
        dropdown.onStateFlagsChanged(_ -> {
            if (dropdown.getSelected() != pos.get()) {
                pos.set(dropdown.getSelected());
                try {
                    Networking.Communication.sendYAMLDefaultHost(
                            YAMLMessage.builder()
                                    .setPacketType(YAMLMessage.PACKET_TYPE.request)
                                    .setRequestType(YAMLMessage.REQUEST_TYPE.menu_change)
                                    .setRequestFile(this.getTitle())
                                    .setObjectPath(_dropdown.getPath())
                                    .setObjectNewValue(String.valueOf(pos.get()))
                                    .build()
                    );
                } catch (ConfigurationException | YAMLSerializer.InvalidReplyTypeException |
                         YAMLSerializer.InvalidPacketTypeException | YAMLSerializer.TODOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void insertDropdown(com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Dropdown dropdown, ExpanderRow expander) {
        expander.addRow(getDropdown(dropdown));
    }

    public void insertDropdown(com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Dropdown dropdown, PreferencesGroup prefGroup) {
        prefGroup.add(getDropdown(dropdown));
    }

    public ActionRow getProperty(com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Property property) {
        var _property = ActionRow.builder()
                .setTitle(property.getLabel())
                .setSubtitle(property.getContent())
                .setCssClasses(new String[]{"property"})
                .build();
        insertSdt(_property, property);
        return _property;
    }

    public void insertProperty(com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Property property, ExpanderRow expander) {
        expander.addRow(getProperty(property));
    }

    public void insertProperty(com.toxicstoxm.LEDSuite.yaml_factory.AnimationMenu.Widgets.Property property, PreferencesGroup prefGroup) {
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

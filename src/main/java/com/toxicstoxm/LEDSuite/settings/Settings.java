package com.toxicstoxm.LEDSuite.settings;

import com.toxicstoxm.LEDSuite.LEDSuite;
import com.toxicstoxm.LEDSuite.event_handling.EventHandler;
import com.toxicstoxm.LEDSuite.event_handling.Events;
import com.toxicstoxm.LEDSuite.event_handling.listener.EventListener;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.configuration2.YAMLConfiguration;

import java.io.IOException;

@Setter
@Getter
public class Settings implements EventListener {
    private Type type = Type.UNDEFINED;
    private String name = "Universal-Config-File";

    public enum Type {
        LOCAL,
        SERVER,
        UNDEFINED
    }
    public void saveDefaultConfig() throws IOException, NullPointerException {}
    public void load(YAMLConfiguration config) {}
    public void save() {}
    public void copy(Settings settings) {
    }
    public void reload(String changes) {
        LEDSuite.eventManager.fireEvent(new Events.Reload(getName() + " changed! Changes: " + changes));
    }
    public Settings cloneS() {
        Settings settings1 = new Settings();
        settings1.copy(this);
        return settings1;
    }
    public void startup() {}

    @EventHandler
    public void onSave(Events.Save e) {
        this.save();
    }
    @EventHandler
    public void onStartup(Events.Startup e) {
        this.startup();
    }
}

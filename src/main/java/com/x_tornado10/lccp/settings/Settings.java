package com.x_tornado10.lccp.settings;

import com.x_tornado10.lccp.LCCP;
import com.x_tornado10.lccp.event_handling.EventHandler;
import com.x_tornado10.lccp.event_handling.Events;
import com.x_tornado10.lccp.event_handling.listener.EventListener;
import lombok.*;
import org.apache.commons.configuration2.FileBasedConfiguration;

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
    public void load(FileBasedConfiguration config) {}
    public void save() {}
    public void copy(Settings settings) {
    }
    public void reload(String changes) {
        LCCP.eventManager.fireEvent(new Events.Reload(getName() + " changed! Changes: " + changes));
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

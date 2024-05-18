package com.x_tornado10.lccp.Settings;

import com.x_tornado10.lccp.Events.EventListener;
import com.x_tornado10.lccp.Events.Events.Event;
import com.x_tornado10.lccp.LCCP;
import lombok.Getter;
import lombok.Setter;
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
    public Settings cloneS() {
        Settings settings1 = new Settings();
        settings1.copy(this);
        return settings1;
    }
    public void startup() {}
    @Override
    public void onEvent(Event event) {
        switch (event.getType()) {
            case SAVE -> this.save();
            case STARTUP -> this.startup();
        }
    }
}

package com.x_tornado10.Settings;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.configuration2.FileBasedConfiguration;

import java.io.IOException;

@Setter
@Getter
public class Settings {
    private Type type = Type.UNDEFINED;
    private String name = "Universal-Config-File";
    public enum Type {
        LOCAL,
        SERVER,
        UNDEFINED
    }
    public void saveDefaultConfig() throws IOException, NullPointerException {}
    public void load(FileBasedConfiguration config) {}
    public void copy(Settings settings) {}
}

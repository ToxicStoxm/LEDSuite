package com.x_tornado10.Settings;

import com.x_tornado10.Main;
import com.x_tornado10.util.Paths;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.configuration2.FileBasedConfiguration;

import java.io.*;
import java.net.URL;
import java.util.Properties;

@Setter
@Getter
public class Settings {
    private boolean DarkM;
    private String DarkMColorPrim;
    private String DarkMColorSec;
    private String LightMColorPrim;
    private String LightMColorSec;
    private String WindowTitle = "LED-Cube-Control-Panel";
    private boolean WindowResizeable;
    private int WindowWidth;
    private int WindowHeight;
    private boolean WindowCenter;
    private int WindowX;
    private int WindowY;
    private boolean FakeLoadingBar;
    public void saveDefaultConfig() throws IOException, NullPointerException {
        URL url = getClass().getClassLoader().getResource("config.yaml");
        if (url == null) throw new NullPointerException();
        try(InputStream inputStream = url.openStream()) {
            File outputFile = new File(Paths.config);
            try (OutputStream outputStream = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        }
    }
    public void load(FileBasedConfiguration config) {
        String version = "";
        this.DarkM = config.getBoolean(Paths.Config.DARK_MODE_ENABLED);
        this.DarkMColorPrim = config.getString(Paths.Config.DARK_MODE_COLOR_PRIMARY);
        this.DarkMColorSec = config.getString(Paths.Config.DARK_MODE_COLOR_SECONDARY);
        this.LightMColorPrim = config.getString(Paths.Config.LIGHT_MODE_COLOR_PRIMARY);
        this.LightMColorSec = config.getString(Paths.Config.LIGHT_MODE_COLOR_SECONDARY);
        try (InputStream inputStream = Main.class.getResourceAsStream("/version.properties")) {
            Properties properties = new Properties();
            properties.load(inputStream);
            version = properties.getProperty("app.version");
        } catch (IOException ignored) {
            Main.logger.error("Wasn't able to get app version!");
            Main.logger.warn("Application was halted!");
            Main.logger.warn("If this keeps happening please open an issue on GitHub!");
            Main.logger.warn("Please restart the application!");
            Main.error();
            return;
        }
        this.WindowTitle = config.getString(Paths.Config.WINDOW_TITLE).replace("%VERSION%", version);
        this.WindowResizeable = config.getBoolean(Paths.Config.WINDOW_RESIZABLE);
        this.WindowWidth = config.getInt(Paths.Config.WINDOW_INITIAL_WIDTH);
        this.WindowHeight = config.getInt(Paths.Config.WINDOW_INITIAL_HEIGHT);
        this.WindowCenter = config.getBoolean(Paths.Config.WINDOW_SPAWN_CENTER);
        this.WindowX = config.getInt(Paths.Config.WINDOW_SPAWN_X);
        this.WindowY = config.getInt(Paths.Config.WINDOW_SPAWN_Y);
        this.FakeLoadingBar = config.getBoolean(Paths.Config.STARTUP_FAKE_LOADING_BAR);
    }
}

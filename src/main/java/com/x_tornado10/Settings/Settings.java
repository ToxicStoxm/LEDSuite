package com.x_tornado10.Settings;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
}

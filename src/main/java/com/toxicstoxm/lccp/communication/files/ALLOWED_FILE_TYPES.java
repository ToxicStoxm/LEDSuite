package com.toxicstoxm.lccp.communication.files;

import java.util.HashSet;
import java.util.Set;

public class ALLOWED_FILE_TYPES {
    private static final Set<String> allowedFileTypes = new HashSet<>();

    static {
        // Adding image file types
        allowedFileTypes.add("avif");
        allowedFileTypes.add("avifs");
        allowedFileTypes.add("bmp");
        allowedFileTypes.add("cgm");
        allowedFileTypes.add("g3");
        allowedFileTypes.add("gif");
        allowedFileTypes.add("heif");
        allowedFileTypes.add("heic");
        allowedFileTypes.add("ief");
        allowedFileTypes.add("jpe");
        allowedFileTypes.add("jpeg");
        allowedFileTypes.add("jpg");
        allowedFileTypes.add("pjpg");
        allowedFileTypes.add("jfif");
        allowedFileTypes.add("jfif-tbnl");
        allowedFileTypes.add("jif");
        allowedFileTypes.add("jfi");
        allowedFileTypes.add("png");
        allowedFileTypes.add("btif");
        allowedFileTypes.add("svg");
        allowedFileTypes.add("svgz");
        allowedFileTypes.add("tif");
        allowedFileTypes.add("tiff");
        allowedFileTypes.add("psd");
        allowedFileTypes.add("djv");
        allowedFileTypes.add("djvu");
        allowedFileTypes.add("dwg");
        allowedFileTypes.add("dxf");
        allowedFileTypes.add("fbs");
        allowedFileTypes.add("fpx");
        allowedFileTypes.add("fst");
        allowedFileTypes.add("mmr");
        allowedFileTypes.add("rlc");
        allowedFileTypes.add("mdi");
        allowedFileTypes.add("npx");
        allowedFileTypes.add("wbmp");
        allowedFileTypes.add("xif");
        allowedFileTypes.add("webp");
        allowedFileTypes.add("dng");
        allowedFileTypes.add("cr2");
        allowedFileTypes.add("crw");
        allowedFileTypes.add("ras");
        allowedFileTypes.add("cmx");
        allowedFileTypes.add("erf");
        allowedFileTypes.add("fh");
        allowedFileTypes.add("fh4");
        allowedFileTypes.add("fh5");
        allowedFileTypes.add("fh7");
        allowedFileTypes.add("fhc");
        allowedFileTypes.add("raf");
        allowedFileTypes.add("icns");
        allowedFileTypes.add("ico");
        allowedFileTypes.add("dcr");
        allowedFileTypes.add("k25");
        allowedFileTypes.add("kdc");
        allowedFileTypes.add("mrw");
        allowedFileTypes.add("nef");
        allowedFileTypes.add("orf");
        allowedFileTypes.add("raw");
        allowedFileTypes.add("rw2");
        allowedFileTypes.add("rwl");
        allowedFileTypes.add("pcx");
        allowedFileTypes.add("pef");
        allowedFileTypes.add("ptx");
        allowedFileTypes.add("pct");
        allowedFileTypes.add("pic");
        allowedFileTypes.add("pnm");
        allowedFileTypes.add("pbm");
        allowedFileTypes.add("pgm");
        allowedFileTypes.add("ppm");
        allowedFileTypes.add("rgb");
        allowedFileTypes.add("x3f");
        allowedFileTypes.add("arw");
        allowedFileTypes.add("sr2");
        allowedFileTypes.add("srf");
        allowedFileTypes.add("xbm");
        allowedFileTypes.add("xpm");
        allowedFileTypes.add("xwd");

        // Adding video file types
        allowedFileTypes.add("3gp");
        allowedFileTypes.add("3g2");
        allowedFileTypes.add("h261");
        allowedFileTypes.add("h263");
        allowedFileTypes.add("h264");
        allowedFileTypes.add("jpgv");
        allowedFileTypes.add("jpgm");
        allowedFileTypes.add("jpm");
        allowedFileTypes.add("mj2");
        allowedFileTypes.add("mjp2");
        allowedFileTypes.add("ts");
        allowedFileTypes.add("mp4");
        allowedFileTypes.add("mp4v");
        allowedFileTypes.add("mpg4");
        allowedFileTypes.add("m1v");
        allowedFileTypes.add("m2v");
        allowedFileTypes.add("mpa");
        allowedFileTypes.add("mpe");
        allowedFileTypes.add("mpeg");
        allowedFileTypes.add("mpg");
        allowedFileTypes.add("ogv");
        allowedFileTypes.add("mov");
        allowedFileTypes.add("qt");
        allowedFileTypes.add("fvt");
        allowedFileTypes.add("m4u");
        allowedFileTypes.add("mxu");
        allowedFileTypes.add("pyv");
        allowedFileTypes.add("viv");
        allowedFileTypes.add("webm");
        allowedFileTypes.add("f4v");
        allowedFileTypes.add("fli");
        allowedFileTypes.add("flv");
        allowedFileTypes.add("m4v");
        allowedFileTypes.add("mkv");
        allowedFileTypes.add("asf");
        allowedFileTypes.add("asx");
        allowedFileTypes.add("wm");
        allowedFileTypes.add("wmv");
        allowedFileTypes.add("wmx");
        allowedFileTypes.add("wvx");
        allowedFileTypes.add("avi");
        allowedFileTypes.add("movie");

        // shared libraries
        allowedFileTypes.add("so");
    }

    public static boolean isALLOWED(String extension) {
        return allowedFileTypes.contains(extension.toLowerCase());
    }
}

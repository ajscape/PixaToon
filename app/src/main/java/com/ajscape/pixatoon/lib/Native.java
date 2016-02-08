package com.ajscape.pixatoon.lib;

/**
 * Native method interface
 */
public class Native {
    public static native void setScaleFactor(double scaleFactor);

    public static native void setSketchTexture(long texMatAddr);

    public static native void setSketchFlip(boolean flip);

    public static native void colorCartoonFilter(long srcMatAddr, long dstMatAddr, int thickness, int threshold);

    public static native void grayCartoonFilter(long srcMatAddr, long dstMatAddr, int thickness, int threshold);

    public static native void pencilSketchFilter(long srcMatAddr, long dstMatAddr, int sketchBlend, int contrast);

    public static native void colorSketchFilter(long srcMatAddr, long dstMatAddr, int sketchBlend, int contrast);

    public static native void pixelArtFilter(long srcMatAddr, long dstMatAddr, int pixelSize, int colorNum);

    public static native void oilPaintFilter(long srcMatAddr, long dstMatAddr, int radius, int levels);
}

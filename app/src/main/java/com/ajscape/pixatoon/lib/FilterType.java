package com.ajscape.pixatoon.lib;

/**
 * Created by AtulJadhav on 9/21/2015.
 */
public enum FilterType {
    COLOR_CARTOON("Color Cartoon"),
    GRAY_CARTOON("Gray Cartoon"),
    PENCIL_SKETCH("Pencil Sketch"),
    COLOR_SKETCH("Color Sketch"),
    PIXEL_ART("Pixel Art"),
    OIL_PAINT("Oil Paint");

    private String value;

    FilterType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}

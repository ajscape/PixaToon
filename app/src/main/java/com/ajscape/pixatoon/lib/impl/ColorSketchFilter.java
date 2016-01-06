package com.ajscape.pixatoon.lib.impl;

import com.ajscape.pixatoon.lib.Filter;
import com.ajscape.pixatoon.lib.FilterType;
import com.ajscape.pixatoon.lib.Native;

import org.opencv.core.Mat;

/**
 * Created by AtulJadhav on 9/20/2015.
 */
public class ColorSketchFilter extends Filter {

    public ColorSketchFilter(FilterType filterType) {
        super(filterType);

        mFilterConfigs.add(new FilterConfig("Blend"));
        mFilterConfigs.add(new FilterConfig("Scale"));
        mDefaultScaleFactor = 0.8;

        resetConfig();
    }

    @Override
    public void process(Mat src, Mat dst) {
        Native.colorSketchFilter(src.getNativeObjAddr(), dst.getNativeObjAddr(), mFilterConfigs.get(0).value, mFilterConfigs.get(1).value);
    }

    @Override
    public void resetConfig() {
        mFilterConfigs.get(0).value = 70;
        mFilterConfigs.get(1).value = 40;
    }
}

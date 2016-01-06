package com.ajscape.pixatoon.lib;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AtulJadhav on 9/20/2015.
 */
public abstract class Filter {

    public class FilterConfig {
        public String name;
        public int value;

        public FilterConfig(String name) {
            this.name = name;
            this.value = 0;
        }
    }

    protected FilterType mFilterType;
    protected double mDefaultScaleFactor;
    protected List<FilterConfig> mFilterConfigs;

    public Filter(FilterType mFilterType) {
        this.mFilterType = mFilterType;
        this.mDefaultScaleFactor = 1.0;
        mFilterConfigs = new ArrayList<>();
    }

    public List<FilterConfig> getFilterConfigs() {
        return mFilterConfigs;
    }

    public FilterType getType() {
        return mFilterType;
    }

    public double getDefaultScaleFactor() {
        return mDefaultScaleFactor;
    }

    public abstract void process(Mat src, Mat dst);

    public abstract void resetConfig();
}

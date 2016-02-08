package com.ajscape.pixatoon.lib;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract Base class for image filter implementation
 */
public abstract class Filter {

    /**
     * Class representing Filter Configuration parameter
     */
    public class FilterConfig {
        public String name;
        public int value;

        public FilterConfig(String name) {
            this.name = name;
            this.value = 0;
        }
    }

    // Filter type
    protected FilterType mFilterType;

    // Filter processing scaling factor (low value will reduce image processing size, reducing processing delay but sacrificing image quality)
    protected double mDefaultScaleFactor;

    protected List<FilterConfig> mFilterConfigs;

    /**
     * Constructor to initialize filter with FilterType enum
     * @param mFilterType
     */
    public Filter(FilterType mFilterType) {
        this.mFilterType = mFilterType;
        this.mDefaultScaleFactor = 1.0;
        mFilterConfigs = new ArrayList<>();
    }

    /**
     * Returns list of filter config params
     * @return
     */
    public List<FilterConfig> getFilterConfigs() {
        return mFilterConfigs;
    }

    /**
     * Returns filter type enum value
     * @return
     */
    public FilterType getType() {
        return mFilterType;
    }

    public double getDefaultScaleFactor() {
        return mDefaultScaleFactor;
    }

    /**
     * Abstract method to apply filter to src, and pass result as dst (to be implemented by derived classes)
     * @param src
     * @param dst
     */
    public abstract void process(Mat src, Mat dst);

    /**
     * Abstract method to reset filter config values to default (to be implemented by derived classes)
     */
    public abstract void resetConfig();
}

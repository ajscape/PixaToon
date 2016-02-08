package com.ajscape.pixatoon.lib;

import android.app.Application;

import com.ajscape.pixatoon.lib.impl.ColorCartoonFilter;
import com.ajscape.pixatoon.lib.impl.ColorSketchFilter;
import com.ajscape.pixatoon.lib.impl.GrayCartoonFilter;
import com.ajscape.pixatoon.lib.impl.OilPaintFilter;
import com.ajscape.pixatoon.lib.impl.PixelArtFilter;
import com.ajscape.pixatoon.lib.impl.PencilSketchFilter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Image Filter Manager (singelton) to control filters configuration and selection from UI
 */

public class FilterManager extends Application {

    private ArrayList<Filter> mFilterList;
    private HashMap<FilterType, Filter> mFilterType2FilterMap;
    private Filter mCurrentFilter;
    private FilterType mDefaultFilterType = FilterType.COLOR_CARTOON;
    private Filter mDefaultFilter;

    // Filter processing scaling factor
    private double mFilterScaleFactor;

    // Flag to check if sketch texture is flipped horizontally
    private boolean mSketchFlip = false;

    // Private static singleton instance
    private static FilterManager sInstance;

    /**
     * Get singleton instance
     * @return
     */
    public static FilterManager getInstance() {
        if(sInstance == null)
            sInstance = new FilterManager();
        return sInstance;
    }

    /**
     * Private constructor
     */
    private FilterManager() {
        mFilterList = new ArrayList<>();
        mFilterType2FilterMap = new HashMap<>();
        mFilterScaleFactor = 1.0;

        // initialize filters and add to list
        buildFilterList();

        // hash filters to map for easy retrieval
        for( Filter filter : mFilterList) {
            mFilterType2FilterMap.put( filter.getType(), filter);
        }

        if(mDefaultFilterType != null)
            mDefaultFilter = mFilterType2FilterMap.get(mDefaultFilterType);
        else
            mDefaultFilter = null;

        mCurrentFilter = mDefaultFilter;
    }

    /**
     * Add implemented filters to filterList
     */
    private void buildFilterList() {
        mFilterList.add( new ColorCartoonFilter(FilterType.COLOR_CARTOON));
        mFilterList.add( new GrayCartoonFilter(FilterType.GRAY_CARTOON));
        mFilterList.add( new ColorSketchFilter(FilterType.COLOR_SKETCH));
        mFilterList.add( new PencilSketchFilter(FilterType.PENCIL_SKETCH));
        mFilterList.add( new PixelArtFilter(FilterType.PIXEL_ART));
        mFilterList.add( new OilPaintFilter(FilterType.OIL_PAINT));
    }

    /**
     * Get current filter
     * @return
     */
    public Filter getCurrentFilter() {
        return mCurrentFilter;
    }

    /**
     * Set current filter with the given filter type
     * @param filterType
     */
    public void setCurrentFilter(FilterType filterType) {
        if(mCurrentFilter!=null)
            mCurrentFilter.resetConfig();
        mCurrentFilter = mFilterType2FilterMap.get(filterType);
    }

    /**
     * Reset filter manager
     */
    public void reset() {
        if(mCurrentFilter!=null)
            mCurrentFilter.resetConfig();
        mCurrentFilter = mDefaultFilter;
        setSketchFlip(false);
    }

    /**
     * Get filter scale factor
     * @return
     */
    public double getFilterScaleFactor() {
        return mFilterScaleFactor;
    }

    /**
     * Set filter processing scaling factor
     * @param scaleFactor
     */
    public void setFilterScaleFactor(double scaleFactor) {
        mFilterScaleFactor = scaleFactor;
        Native.setScaleFactor(mFilterScaleFactor);
    }

    /**
     * Horizontally flip sketch texture
     * (Only required if gallery image is landscape, and is rotated by pictureView for better screen coverage)
     * @param flip
     */
    public void setSketchFlip(boolean flip) {
        if(mSketchFlip != flip) {
            mSketchFlip = flip;
            Native.setSketchFlip(flip);
        }
    }

}



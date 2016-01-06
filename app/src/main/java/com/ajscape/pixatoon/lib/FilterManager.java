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
 * Created by AtulJadhav on 9/20/2015.
 */

public class FilterManager extends Application {

    private ArrayList<Filter> mFilterList;
    private HashMap<FilterType, Filter> mFilterType2FilterMap;
    private Filter mCurrentFilter;
    private double mFilterScaleFactor;
    private FilterType mDefaultFilterType = FilterType.COLOR_CARTOON;
    private Filter mDefaultFilter;
    private boolean mSketchFlip = false;

    private static FilterManager sInstance;

    public static FilterManager getInstance() {
        if(sInstance == null)
            sInstance = new FilterManager();
        return sInstance;
    }

    private FilterManager() {
        mFilterList = new ArrayList<>();
        mFilterType2FilterMap = new HashMap<>();
        mFilterScaleFactor = 1.0;

        // initialize filters and add to list
        buildFilterList();

        // hash filters to maps for easy retreival
        for( Filter filter : mFilterList) {
            mFilterType2FilterMap.put( filter.getType(), filter);
        }

        if(mDefaultFilterType != null)
            mDefaultFilter = mFilterType2FilterMap.get(mDefaultFilterType);
        else
            mDefaultFilter = null;

        mCurrentFilter = mDefaultFilter;
    }

    private void buildFilterList() {
        mFilterList.add( new ColorCartoonFilter(FilterType.COLOR_CARTOON));
        mFilterList.add( new GrayCartoonFilter(FilterType.GRAY_CARTOON));
        mFilterList.add( new ColorSketchFilter(FilterType.COLOR_SKETCH));
        mFilterList.add( new PencilSketchFilter(FilterType.PENCIL_SKETCH));
        mFilterList.add( new PixelArtFilter(FilterType.PIXEL_ART));
        mFilterList.add( new OilPaintFilter(FilterType.OIL_PAINT));
    }

    public Filter getCurrentFilter() {
        return mCurrentFilter;
    }

    public void setCurrentFilter(FilterType filterType) {
        if(mCurrentFilter!=null)
            mCurrentFilter.resetConfig();
        mCurrentFilter = mFilterType2FilterMap.get(filterType);
    }

    public void reset() {
        if(mCurrentFilter!=null)
            mCurrentFilter.resetConfig();
        mCurrentFilter = mDefaultFilter;
        setSketchFlip(false);
    }

    public double getFilterScaleFactor() {
        return mFilterScaleFactor;
    }

    public void setFilterScaleFactor(double scaleFactor) {
        mFilterScaleFactor = scaleFactor;
        Native.setScaleFactor(mFilterScaleFactor);
    }

    public void setSketchFlip(boolean flip) {
        if(mSketchFlip != flip) {
            mSketchFlip = flip;
            Native.setSketchFlip(flip);
        }
    }

}



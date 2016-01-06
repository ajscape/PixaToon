package com.ajscape.pixatoon.ui.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;

import com.ajscape.pixatoon.R;
import com.ajscape.pixatoon.lib.Filter;
import com.ajscape.pixatoon.lib.FilterManager;
import com.ajscape.pixatoon.lib.FilterType;
import com.ajscape.pixatoon.ui.MainActivity;
import com.ajscape.pixatoon.ui.interfaces.FilterSelectorListener;

import java.util.HashMap;
import java.util.Map;


public class FilterSelectorFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "FilterSelectorFragment:";
    private FilterSelectorListener mCallback;
    private Map<Integer,FilterType> mFilterMap;
    private FilterManager mFilterManager;
    private int mLastScrollPosition = 0;
    private HorizontalScrollView mScrollBar;
    private View mCurrentFilterBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCallback = (FilterSelectorListener) getActivity();
        mFilterManager = FilterManager.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_filterselector, container, false);
        mFilterMap = new HashMap<>();
        initFilterMap();

        for(int filterBtnId : mFilterMap.keySet()) {
            View filterSelectBtn = view.findViewById(filterBtnId);
            filterSelectBtn.setOnClickListener(this);
            if(((MainActivity)getActivity()).getOrientation() == Configuration.ORIENTATION_LANDSCAPE)
                filterSelectBtn.setRotation(90);
        }

        mScrollBar = (HorizontalScrollView)view.findViewById(R.id.scrollBar);
        return view;
    }

    private void initFilterMap() {
        mFilterMap.put(R.id.colorCartoonFilterBtn, FilterType.COLOR_CARTOON);
        mFilterMap.put(R.id.grayCartoonFilterBtn, FilterType.GRAY_CARTOON);
        mFilterMap.put(R.id.colorSketchFilterBtn, FilterType.COLOR_SKETCH);
        mFilterMap.put(R.id.pencilSketchFilterBtn, FilterType.PENCIL_SKETCH);
        mFilterMap.put(R.id.pixelArtFilterBtn, FilterType.PIXEL_ART);
        mFilterMap.put(R.id.oilPaintFilterBtn, FilterType.OIL_PAINT);
    }

    @Override
    public void onPause() {
        super.onPause();
        mLastScrollPosition = mScrollBar.getScrollX();
    }

    @Override
    public void onResume() {
        super.onResume();
        Filter currentFilter = mFilterManager.getCurrentFilter();
        if(currentFilter != null) {
            for(int filterBtnId : mFilterMap.keySet()) {
                if(mFilterMap.get(filterBtnId) == currentFilter.getType()) {
                    Log.d(TAG, "Last selected filter - " + currentFilter.getType());

                    mCurrentFilterBtn = getView().findViewById(filterBtnId);
                    mCurrentFilterBtn.setBackgroundResource(R.color.foreground);
                    break;
                }
            }
        }
        else {
            if(mCurrentFilterBtn != null) {
                mCurrentFilterBtn.setBackgroundResource(R.color.transparent);
            }
            mLastScrollPosition = 0;
            mCurrentFilterBtn = null;
        }
        mScrollBar.post(new Runnable() {
            @Override
            public void run() {
                mScrollBar.setScrollX(mLastScrollPosition);
            }
        });
    }

    @Override
    public void onClick(View view) {
        int filterBtnId = view.getId();
        if(mCurrentFilterBtn != null)
            mCurrentFilterBtn.setBackgroundResource(R.color.transparent);
        mCurrentFilterBtn = view.findViewById(filterBtnId);
        mCurrentFilterBtn.setBackgroundResource(R.color.foreground);

        FilterType filterType = mFilterMap.get(filterBtnId);
        mCallback.onFilterSelect(filterType);
        Log.d(TAG, filterType + "Filter selected");
    }

    public void changeOrientation(int orientation) {

        for(int filterBtnId : mFilterMap.keySet()) {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE)
                getView().findViewById(filterBtnId).setRotation(90);
            else if (orientation == Configuration.ORIENTATION_PORTRAIT)
                getView().findViewById(filterBtnId).setRotation(0);
        }
    }
}

package com.ajscape.pixatoon.ui.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ajscape.pixatoon.R;
import com.ajscape.pixatoon.lib.Filter;
import com.ajscape.pixatoon.ui.interfaces.FilterConfigListener;

import java.util.List;

/**
 * Filter Configuration Panel implemented as fragment
 */
public class FilterConfigFragment extends Fragment {

    private List<Filter.FilterConfig> mFilterConfigs;
    private String mFilterName;
    private TextView mHeaderTextView;
    private TextView [] mConfigTextViews;
    private SeekBar [] mConfigSeekBars;
    private FilterConfigListener mCallback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCallback = (FilterConfigListener)getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_filterconfig, container, false);

        mConfigTextViews = new TextView[2];
        mConfigSeekBars = new SeekBar[2];

        mHeaderTextView = (TextView)view.findViewById(R.id.header);
        mConfigTextViews[0] = (TextView) view.findViewById(R.id.config1Name);
        mConfigSeekBars[0] = (SeekBar) view.findViewById(R.id.config1SeekBar);
        mConfigTextViews[1] = (TextView)view.findViewById(R.id.config2Name);
        mConfigSeekBars[1] = (SeekBar)view.findViewById(R.id.config2SeekBar);

        mHeaderTextView.setText(mFilterName + " Filter Settings");
        for(int i=0; i<2; i++) {
            Filter.FilterConfig config = mFilterConfigs.get(i);
            mConfigTextViews[i].setText(config.name);
            mConfigSeekBars[i].setProgress(config.value);
            mConfigSeekBars[i].setOnSeekBarChangeListener(new ConfigSeekBarListener(config));
        }

        return view;
    }

    public void setFilter(Filter filter) {
        mFilterName = filter.getType().toString();
        mFilterConfigs = filter.getFilterConfigs();
    }

    class ConfigSeekBarListener implements SeekBar.OnSeekBarChangeListener {

        Filter.FilterConfig config;

        ConfigSeekBarListener(Filter.FilterConfig config) {
            this.config = config;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            config.value = progress;
            mCallback.onFilterConfigChanged();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    }
}

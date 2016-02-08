package com.ajscape.pixatoon.ui.interfaces;

import com.ajscape.pixatoon.lib.FilterType;

/**
 * Filter Selector Listener interface
 */
public interface FilterSelectorListener {

    /**
     * Callback method on selecting new filter from selector panel. The new filter type is passed as parameter.
     * @param filterType
     */
    void onFilterSelect(FilterType filterType);
}

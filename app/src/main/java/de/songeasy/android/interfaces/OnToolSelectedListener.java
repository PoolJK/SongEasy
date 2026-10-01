package de.songeasy.android.interfaces;

/**
 * Created by krein on 15.04.2015.
 */

/** Listener for selection of tool in ToolBar*/
public interface OnToolSelectedListener {

    /**
     * Signaling check of tool bar button
     *
     * @param buttonEvent
     *            event of checked button
     */
    abstract void onToolSelected(int buttonEvent);
}

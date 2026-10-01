package de.songeasy.android.interfaces;

/**
 * Created by krein on 15.04.2015.
 */

/** Listener for deselection of tool in ToolBar */
public interface OnToolDeselectedListener {

    /**
     * Signaling uncheck of tool bar button
     *
     * @param buttonEvent
     *            event of unchecked button
     */
    abstract void onToolDeselected(int buttonEvent);
}

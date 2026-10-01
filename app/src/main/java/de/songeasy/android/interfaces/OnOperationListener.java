package de.songeasy.android.interfaces;

/**
 * Created by krein on 15.04.2015.
 */

/** Listener for selection of tool in OperationBar */
public interface OnOperationListener {
        /**
         * Signaling click of execution bar button
         *
         * @param buttonEvent
         *            event of clicked button
         */
        abstract void onOperate(int buttonEvent);
}

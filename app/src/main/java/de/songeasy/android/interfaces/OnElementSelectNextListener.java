package de.songeasy.android.interfaces;

import de.songeasy.android.score.Measure;

/**
 * Listener for selecting next element realative to current selected one up,
 * down, left or right
 */
public interface OnElementSelectNextListener {

	abstract void onSelectNext(Measure.Direction direction);
}

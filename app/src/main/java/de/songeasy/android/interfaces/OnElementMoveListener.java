package de.songeasy.android.interfaces;

import de.songeasy.android.score.Measure;

/** Listener for moving current selected element */
public interface OnElementMoveListener {

	abstract void onMove(Measure.Direction direction);
}

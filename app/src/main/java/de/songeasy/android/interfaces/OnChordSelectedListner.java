package de.songeasy.android.interfaces;

import de.songeasy.android.score.Chord;

/**
 * Interface for chord selected event.
 * 
 * @author krein
 * 
 */
public interface OnChordSelectedListner {

	/**
	 * Event handler for user finished selection of chord parameters.
	 * 
	 * @param chord
	 *            Chord with selected parameters
	 */
	abstract void onChordSelected(Chord chord);
}

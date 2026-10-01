package de.songeasy.android.interfaces;

import de.songeasy.android.score.TimedElement;

/** Listener for selection of note value */
public interface OnLengthSelectedListener {

	/**
	 * Event handler for user finished selection of length of timed element,
	 * 
	 * @param length
	 *            Length of selected timed element
	 */
	abstract void onLengthSelected(TimedElement.NoteLength length);
}

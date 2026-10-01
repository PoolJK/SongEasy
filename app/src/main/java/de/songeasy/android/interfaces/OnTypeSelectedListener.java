package de.songeasy.android.interfaces;

import de.songeasy.android.score.ScoreElement;


/** Listener for selection of editing type */
public interface OnTypeSelectedListener {

	abstract void onTypeSelected(Class<? extends ScoreElement> type);
}

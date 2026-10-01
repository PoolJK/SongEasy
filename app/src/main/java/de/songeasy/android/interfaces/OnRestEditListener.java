package de.songeasy.android.interfaces;

import de.songeasy.android.score.Rest;

/**
 * Interface to call listener after editing a rest sign.
 * 
 * @author krein
 * 
 */
public interface OnRestEditListener {
	/**
	 * Listener gets called when rest is edited.
	 * 
	 * @param rest
	 *            edited rest, null if nothing was changed
	 */
	abstract void onRestEdit(Rest rest);
}

/*
 * Copyright (C) 2014 Joerg Krein
 */
package de.songeasy.android.fileactivity;

import android.app.Activity;
import android.content.Intent;

/**
 * A file browser for loading files. Returns with by user selected file.
 * 
 * @author krein
 * 
 */
public class ActivityFileLoadBrowser extends ActivityFileBrowser {

	// string with class name for logging
	private final String TAG = this.getClass().getSimpleName();


	@Override
	public void onFileSelected() {

		final Intent intent = new Intent();
		intent.putExtra(KEY_SELECTED_FILE, selectedFile);

		if (getParent() == null) {
			setResult(Activity.RESULT_OK, intent);
		} else {
			getParent().setResult(Activity.RESULT_OK, intent);
		}

		finish();
	}

} // class


/*
 * Copyright (C) 2013 Joerg Krein
 */

package de.songeasy.android;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

/***
 * PreferenceActivity is a built-in Activity for preferences management
 * 
 * The summary of list preferences is updated on prefenced change. So that the
 * user gets info about the current selected values.
 * 
 * To retrieve the values stored by this activity in other activities use the
 * following snippet:
 * 
 * SharedPreferences sharedPreferences =
 * PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
 * <Preference Type> preferenceValue = sharedPreferences.get<Preference
 * Type>("<Preference Key>",<default value>);
 * 
 * @see <a href= "http://stackoverflow.com/q/531427" stackoverflow </a>
 */
public class ActivityEditPreferences extends PreferenceActivity implements
		SharedPreferences.OnSharedPreferenceChangeListener {

	// string with class name for logging
	private final String TAG = this.getClass().getSimpleName();

	// list with ListPreferences of this PreferenceActivity
	private ArrayList<ListPreference> mListPreferences;

	// list with keys of ListPreferences
	private String[] mListPreferencesKeys;

	private EditTextPreference mEditTextPreference;


	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		SharedPreferences sharedPrefs = getPreferenceManager()
				.getSharedPreferences();

		// register for onChange event, for updating the summary of
		// ListPreferences
		sharedPrefs.registerOnSharedPreferenceChangeListener(this);

		// get reference to EditText preference
		mEditTextPreference = (EditTextPreference) getPreferenceManager()
				.findPreference(
						getResources().getString(R.string.pref_key_composer));

		mEditTextPreference.setSummary(sharedPrefs.getString(getResources()
				.getString(R.string.pref_key_composer), ""));
	}


	/*
	 * update summary (text below view that shows the current selection) of
	 * ListPreferences
	 */
	public void onSharedPreferenceChanged(SharedPreferences pref, String prefKey) {

		// update summary of EditTestPreferences
		if (prefKey
				.equals(getResources().getString(R.string.pref_key_composer))) {
			mEditTextPreference.setSummary(mEditTextPreference.getText());
		}

	}
}
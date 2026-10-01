/*
 * Copyright (C) 2012 Joerg Krein
 */
package de.songeasy.android.fileactivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import de.songeasy.android.R;
import de.songeasy.android.SpinnerUtil;
import de.songeasy.android.interfaces.SpinnerItemSelectedListener;
import de.songeasy.android.score.KeySignature;
import de.songeasy.android.score.KeySignature.KeyName;
import de.songeasy.android.score.Song;
import de.songeasy.android.score.TimeSignature;

/**
 * Activity that queries required properties for creating a new song.
 * 
 * @author krein
 * 
 */
public class ActivityFileNew extends Activity {

	// string with class name for logging
	private final String TAG = this.getClass().getSimpleName();

	/** Keys for identifying values in an intent */
	public static final String KEY_SONG_NAME = "SongName";
	public static final String KEY_COMPOSER_NAME = "ComposerName";
	public static final String KEY_KEY_SIGNATURE = "SongKey";
	public static final String KEY_TIME_SIGNATURE = "SongTimeSignature";
	public static final String KEY_LAYOUT = "SongLayout";
	public static final String KEY_MODE = "Mode";

	// private static final String[] times = new String[] { "1", "2", "3", "4",
	// "6", "8", "16", "32" };
	private static final Integer[] timesValues = { 1, 2, 3, 4, 8, 12, 16 };

	/** mode this this activity can be opened in */
	public static enum Mode {
		NEW, EDIT
	};

	// * edit or new file mode */
	private Mode mode;
	// * whether we were canceled or not */
	private Boolean canceled = true;

	private String fileName;
	private String composerName;
	private KeySignature key;
	private TimeSignature time;
	private Song.Layout layout;


	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.lay_activity_file_new);

		// set title of activity in actionbar
		getActionBar().setSubtitle(
				this.getResources().getString(R.string.title_new_song));

		// get parameters for this activity
		Bundle bundle = getIntent().getExtras();

		// either mode must be chosen
		if (!bundle.containsKey(KEY_MODE))
			finish();

		// set picker for key signature spinner
		// API11 Class?
		NumberPicker pickerKey = (NumberPicker) findViewById(R.id.pickerNewSongKey);
		String[] arrays = getResources().getStringArray(R.array.spinner_keys);
		pickerKey.setDisplayedValues(arrays);
		pickerKey.setMinValue(0);
		pickerKey.setMaxValue(getResources().getStringArray(
				R.array.spinner_keys).length - 1);
		pickerKey
				.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

		// set adapter for time signature spinners
		Spinner spinnerNumerator = (Spinner) findViewById(R.id.spinnerTimeNumerator);
		Spinner spinnerDenominator = (Spinner) findViewById(R.id.spinnerTimeDenominator);
		ArrayAdapter<Integer> timeAdapter = new ArrayAdapter<Integer>(this,
				android.R.layout.simple_list_item_1, timesValues);
		spinnerNumerator.setAdapter(timeAdapter);
		spinnerDenominator.setAdapter(timeAdapter);

		// set adapter for number of systems spinner
		Spinner spinnerSystems = SpinnerUtil.createNewSpinner(
				findViewById(android.R.id.content), R.id.spinnerNumberSystems,
				getResources().getStringArray(R.array.spinner_systems),
				Song.Layout.values(),
				new SpinnerItemSelectedListener<Song.Layout>() {

					public void onItemSelected(Spinner item, Song.Layout value) {
						layout = value;
					};
				});

		// set EditText for song name and composer
		EditText editTextSongName = ((EditText) findViewById(R.id.editTextNewSongName));
		EditText editTextComposer = ((EditText) findViewById(R.id.editTextNewSongComposerName));

		// register onClick for Ok button
		((Button) findViewById(R.id.button_ok))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						canceled = false;
						finish();
					}
				});

		((Button) findViewById(R.id.button_cancel))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						canceled = true;
						finish();
					}
				});

		// select view for entering song name
		((EditText) findViewById(R.id.editTextNewSongName)).requestFocus();

		mode = (Mode) bundle.getSerializable(KEY_MODE);

		if (mode == Mode.EDIT) {
			pickerKey.setEnabled(false);
			spinnerNumerator.setEnabled(false);
			spinnerDenominator.setEnabled(false);
			spinnerSystems.setEnabled(false);
		}

		// set song name from bundel
		if (bundle.containsKey(KEY_SONG_NAME)) {
			editTextSongName.setText(bundle.getString(KEY_SONG_NAME));

		} else {
			// set new song default name
			editTextSongName.setHint(this.getResources().getString(
					R.string.new_song_default_name));
		}
		// set composer name from bundle
		if (bundle.containsKey(KEY_COMPOSER_NAME)) {
			editTextComposer.setText(bundle.getString(KEY_COMPOSER_NAME));

		} else {
			// set composer default name
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(this);
			editTextComposer.setHint(prefs.getString(
					getString(R.string.pref_key_composer), ""));

		}
		// set time signature from bundle
		if (bundle.containsKey(KEY_TIME_SIGNATURE)) {
			spinnerNumerator.setSelection(((TimeSignature) bundle
					.getParcelable(KEY_TIME_SIGNATURE)).getNumerator());
			spinnerDenominator.setSelection(((TimeSignature) bundle
					.getParcelable(KEY_TIME_SIGNATURE)).getDenominator());

		} else {
			// preselect 4/4 time signature
			spinnerNumerator.setSelection(3);
			spinnerDenominator.setSelection(3);
		}

		// set key signature from bundle
		if (bundle.containsKey(KEY_KEY_SIGNATURE)) {
			pickerKey.setValue(((KeySignature) bundle
					.getParcelable(KEY_KEY_SIGNATURE)).getKeyName().ordinal());
		} else {
			// preselect C major
			pickerKey.setValue(9);
		}

	}


	@Override
	public void finish() {

		if (canceled == false) {
			// get songname
			fileName = ((TextView) findViewById(R.id.editTextNewSongName))
					.getText().toString();

			// set default if user didn't entered name
			if (fileName.length() < 2)
				fileName = getResources().getString(
						R.string.new_song_default_name);

			// get composer name
			composerName = ((EditText) findViewById(R.id.editTextNewSongComposerName))
					.getText().toString();

			// try to set default if none is entered
			if (composerName.length() < 2) {

				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(this);
				composerName = prefs.getString(
						getString(R.string.pref_key_composer), "");

			}

			// get song key
			NumberPicker pickerKey = (NumberPicker) findViewById(R.id.pickerNewSongKey);
			key = new KeySignature(KeyName.values()[(int) pickerKey.getValue()]);

			// get song time
			Spinner spinnerNumerator = (Spinner) findViewById(R.id.spinnerTimeNumerator);
			Spinner spinnerDenominator = (Spinner) findViewById(R.id.spinnerTimeDenominator);

			time = new TimeSignature(
					(Integer) spinnerNumerator.getSelectedItem(),
					(Integer) spinnerDenominator.getSelectedItem());

			// TODO: check for completeness of selected values

			// Prepare data intent
			Intent data = new Intent();
			data.putExtra(KEY_SONG_NAME, fileName);
			data.putExtra(KEY_COMPOSER_NAME, composerName);
			data.putExtra(KEY_KEY_SIGNATURE, key);
			data.putExtra(KEY_TIME_SIGNATURE, time);
			data.putExtra(KEY_LAYOUT, layout);

			setResult(RESULT_OK, data);

		} else {
			setResult(RESULT_CANCELED);
		}
		// clear actionbar file name dispay
		getActionBar().setSubtitle("");
		super.finish();
	}
}

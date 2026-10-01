/*
 * Copyright (C) 2015 Joerg Krein
 */

package de.songeasy.android.dialog;

import java.util.Arrays;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import de.songeasy.android.R;
import de.songeasy.android.interfaces.OnChordSelectedListner;
import de.songeasy.android.score.Chord;

/**
 * Dialog to select chord parameters. Shows a dialog with editing elements to
 * let the user choose the chord parameters.
 * 
 * @author krein
 */
public class DialogEditChord extends Dialog {

	// string with class name for logging
	private final String TAG = this.getClass().getSimpleName();

	private Context context;

	// chord to edit
	private Chord chord;

	/** Picker with base note of chord */
	private NumberPicker rootPicker;
	/** Picker with base accidental */
	private NumberPicker rootAccidentalPicker;
	/** Picker with chord type */
	private NumberPicker kindPicker;
	/** Picker with optional bass note */
	private NumberPicker bassPicker;
	/** Picker with bass accidental */
	private NumberPicker bassAccidentalPicker;

	private OnChordSelectedListner selectedListener;


	/**
	 * Set listener for chord selected event.
	 * 
	 * @param listener
	 */
	public void setOnChordSelectedListener(OnChordSelectedListner listener) {

		selectedListener = listener;
	}


	/**
	 * Constructor for dialog.
	 * 
	 * @param context
	 *            Context of calling activity.
	 * @param chord
	 *            Chord with parameters to edit.
	 */
	public DialogEditChord(Context context, Chord chord) {

		super(context);

		this.context = context;
		this.chord = chord;
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		int len;

		setContentView(R.layout.lay_dlg_edit_chords);
		// setTitle(context.getString(R.string.title_edit_chords));
		// requestWindowFeature(Window.FEATURE_NO_TITLE);

		rootPicker = ((NumberPicker) findViewById(R.id.pickerChordRoot));
		rootPicker.setDisplayedValues(Chord.ROOT);
		len = Chord.ROOT.length - 1;
		rootPicker.setMaxValue(len);
		rootPicker.setMinValue(0);
		rootPicker
				.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

		rootAccidentalPicker = ((NumberPicker) findViewById(R.id.pickerChordRootAccidental));
		rootAccidentalPicker.setDisplayedValues(Chord.ACCIDENTAL);
		len = Chord.ACCIDENTAL.length - 1;
		rootAccidentalPicker.setMaxValue(len);
		rootAccidentalPicker.setMinValue(0);
		rootAccidentalPicker
				.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

		kindPicker = ((NumberPicker) findViewById(R.id.pickerChordKind));
		kindPicker.setDisplayedValues(Chord.Kind.getNames());
		len = Chord.Kind.getNames().length - 1;
		kindPicker.setMaxValue(len);
		kindPicker.setMinValue(0);
		// kindPicker.setWrapSelectorWheel(false);
		kindPicker
				.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

		bassPicker = ((NumberPicker) findViewById(R.id.pickerChordBass));
		bassPicker.setDisplayedValues(Chord.BASS);
		len = Chord.BASS.length - 1;
		bassPicker.setMaxValue(len);
		bassPicker.setMinValue(0);
		bassPicker
				.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

		bassAccidentalPicker = ((NumberPicker) findViewById(R.id.pickerChordBassAccidental));
		bassAccidentalPicker.setDisplayedValues(Chord.ACCIDENTAL);
		len = Chord.ACCIDENTAL.length - 1;
		bassAccidentalPicker.setMaxValue(len);
		bassAccidentalPicker.setMinValue(0);
		bassAccidentalPicker
				.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

		// show current values of chord
		setSelection();

		Button buttonOk = (Button) findViewById(R.id.button_ok);
		buttonOk.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				// update chord with user selected parameters
				int idx;
				String val;
				Chord.Kind kind;

				idx = rootPicker.getValue();
				val = Chord.ROOT[idx];
				chord.setRoot(val);

				idx = rootAccidentalPicker.getValue();
				val = Chord.ACCIDENTAL[idx];
				// set to empty string if no root accidental is selected
				if (val.contentEquals(chord.ACCIDENTAL[0]))
					val = "";
				chord.setRootAccidental(val);

				idx = kindPicker.getValue();
				kind = Chord.Kind.values()[idx];
				chord.setKind(kind);

				val = Chord.BASS[bassPicker.getValue()];
				// set empty string if no bass note is selected
				if (val.contentEquals(Chord.BASS[0]))
					val = "";
				chord.setBass(val);

				val = Chord.ACCIDENTAL[bassAccidentalPicker.getValue()];
				// set to empty string if no bass accidental is selected
				if (val.contentEquals(Chord.ACCIDENTAL[0]))
					val = "";
				chord.setBassAccidental(val);

				if (selectedListener != null)
					selectedListener.onChordSelected(chord);

				dismiss();
			}
		});

		((Button) findViewById(R.id.button_cancel))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (selectedListener != null)
							selectedListener.onChordSelected(null);
						cancel();
					}
				});
	}


	/**
	 * Set preselection of chord pickers.
	 */
	private void setSelection() {

		int index = 0;

		index = Arrays.asList(Chord.ROOT).indexOf(chord.getRoot());
		rootPicker.setValue(index);

		if (chord.getRootAccidental().isEmpty())
			rootAccidentalPicker.setValue(0);
		else {
			index = Arrays.asList(Chord.ACCIDENTAL).indexOf(
					chord.getRootAccidental());
			rootAccidentalPicker.setValue(index);
		}

		index = chord.getKind().ordinal();
		kindPicker.setValue(index);

		if (chord.getBass().isEmpty())
			bassPicker.setValue(0);
		else {
			index = Arrays.asList(Chord.BASS).indexOf(chord.getBass());
			bassPicker.setValue(index);
		}

		if (chord.getBassAccidental().isEmpty())
			bassAccidentalPicker.setValue(0);
		else {
			index = Arrays.asList(Chord.ACCIDENTAL).indexOf(
					chord.getBassAccidental());
			bassAccidentalPicker.setValue(index);
		}

	}
} // class

/*
 * Copyright (C) 2012 Joerg Krein
 */
package de.songeasy.android.dialog;

import java.util.Arrays;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.Spinner;
import de.songeasy.android.R;
import de.songeasy.android.SpinnerUtil;
import de.songeasy.android.interfaces.OnMeasureSetupListener;
import de.songeasy.android.interfaces.SpinnerItemSelectedListener;
import de.songeasy.android.score.KeySignature;
import de.songeasy.android.score.KeySignature.KeyName;
import de.songeasy.android.score.Measure;
import de.songeasy.android.score.Measure.MeasureAttribute;
import de.songeasy.android.score.Measure.RepeatType;
import de.songeasy.android.score.TimeSignature;

public class DialogEditMeasure extends Dialog {

	// string with class name for logging
	private final String TAG = this.getClass().getSimpleName();

	/** mode this dialog can be opened in */
	public static enum Mode {
		ADD, EDIT
	};

	/** Keys for identifying values in a bundle */
	public static final String KEY_MEASURE_COUNT = "MeasureCounts";
	public static final String KEY_MEASURE_NEWLINE = "MeasureNewLine";
	public static final String KEY_MEASURE_REPEAT_START = "MeasureRepeatStart";
	public static final String KEY_MEASURE_REPEAT_STOP = "MeasureRepeatStop";
	public static final String KEY_MEASURE_REPEAT_ENDING = "MeasureRepeatEnding";
	public static final String KEY_MEASURE_KEYCHANGE = "MeasureKeyChange";
	public static final String KEY_MEASURE_KEYSIGNATURE = "MeasureKeySignature";
	public static final String KEY_MEASURE_TIMECHANGE = "MeasureTimeChange";
	public static final String KEY_MEASURE_TIMESIGNATURE = "MeasureTimeSignature";
	public static final String KEY_MEASURE_PAGEBREAK = "MeasurePageBreak";

	/** number of measures to add */
	private static final int minMeasures = 1;
	private static final int maxMeasures = 8;
	/** numerator and denominator of times */
	private static final Integer[] timesValues = { 1, 2, 3, 4, 8, 12, 16 };
	private static final Integer[] systemValues = { 1, 2, 3, 4, 5, 6, 7, 8 };

	private Context context;

	private OnMeasureSetupListener setupListener = null;
	private KeyName keyNameSelected = KeyName.CMAJOR;
	private int defaultNumerator = 4;
	private int defaultDenominator = 4;
	private KeyName defaultKeyName = KeyName.CMAJOR;
	private boolean newLine = false;
	private boolean repeatStart = false;
	private boolean repeatStop = false;
	private boolean repeatEnding = false;
	private boolean pageBreak = false;
	private RepeatType repeatType = RepeatType.NO_REPEAT;
	private Mode mode = Mode.ADD;


	/**
	 * Add listener for measure setup finished.
	 * 
	 * @param onMeasureSetupListener
	 *            Listener for measure setup.
	 */
	public void setOnMeasureSetupListener(
			OnMeasureSetupListener onMeasureSetupListener) {

		setupListener = onMeasureSetupListener;
	}


	/**
	 * Constructor of dialog.
	 * 
	 * @param measureDefault
	 *            previous measure with values to set as defaults
	 * @param context
	 */
	public DialogEditMeasure(Context context, Mode mode, Measure measureDefault) {

		super(context);

		this.context = context;
		this.mode = mode;

		// get values from default measure
		defaultDenominator = measureDefault.getStaves().get(0)
				.getTimeSignature().getDenominator();

		defaultNumerator = measureDefault.getStaves().get(0).getTimeSignature()
				.getNumerator();

		defaultKeyName = measureDefault.getStaves().get(0).getKeySignature()
				.getKeyName();

		// take default values for repeat and newline from default measure only
		// in mode edit
		if (mode == Mode.EDIT) {
			if (measureDefault.getAttributes().contains(
					MeasureAttribute.LINE_BREAK))
				newLine = true;
			else
				newLine = false;

			if (measureDefault.getAttributes().contains(
					MeasureAttribute.PAGE_BREAK))
				pageBreak = true;
			else
				pageBreak = false;

			switch (measureDefault.getRepeat())
				{

				case START:
					repeatStart = true;
					repeatStop = false;
					repeatEnding = false;
					break;

				case STOP:
					repeatStart = false;
					repeatStop = true;
					repeatEnding = false;
					break;

				case BOTH:
					repeatStart = true;
					repeatStop = true;
					repeatEnding = false;
					break;

				case NO_REPEAT:
					repeatStart = false;
					repeatStop = false;
					repeatEnding = false;
					break;

				case ENDING:
					repeatStart = false;
					repeatStop = false;
					repeatEnding = true;
					break;

				default:
					repeatStart = false;
					repeatStop = false;
					repeatEnding = false;
					break;
				}
		} else {
			// disable features for add mode
			newLine = false;
			repeatStart = false;
			repeatStop = false;
			repeatEnding = false;
		}
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.lay_dlg_edit_measure);
		// setTitle(context.getString(R.string.title_edit_measure));

		// setup measure counts picker
		NumberPicker pickerCount = (NumberPicker) findViewById(R.id.pickerMeasureCount);
		pickerCount.setMinValue(minMeasures);
		pickerCount.setMaxValue(maxMeasures);
		pickerCount
				.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

		// setup checkboxes with values from given measure
		((CheckBox) findViewById(R.id.checkBoxNewLine)).setChecked(newLine);
		((CheckBox) findViewById(R.id.checkBoxRepeatStart))
				.setChecked(repeatStart);
		((CheckBox) findViewById(R.id.checkBoxRepeatStop))
				.setChecked(repeatStop);
		((CheckBox) findViewById(R.id.checkBoxRepeatEnding))
				.setChecked(repeatEnding);
		((CheckBox) findViewById(R.id.checkBoxPageBreak)).setChecked(pageBreak);

		((CheckBox) findViewById(R.id.checkBoxRepeatStart))
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// uncheck ending if start or stop is checked
						if (isChecked) {
							((CheckBox) findViewById(R.id.checkBoxRepeatEnding))
									.setChecked(false);
						}

					}
				});

		((CheckBox) findViewById(R.id.checkBoxRepeatStop))
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// uncheck ending if start or stop is checked
						if (isChecked) {
							((CheckBox) findViewById(R.id.checkBoxRepeatEnding))
									.setChecked(false);
						}

					}
				});

		((CheckBox) findViewById(R.id.checkBoxRepeatEnding))
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// uncheck start and stop if ending is checked
						if (isChecked) {
							((CheckBox) findViewById(R.id.checkBoxRepeatStart))
									.setChecked(false);
							((CheckBox) findViewById(R.id.checkBoxRepeatStop))
									.setChecked(false);
						}

					}
				});

		// setup key signature spinner and set to values from previous measure
		Spinner spinnerKey = SpinnerUtil.createNewSpinner(
				findViewById(android.R.id.content), R.id.spinnerNewSongKey,
				context.getResources().getStringArray(R.array.spinner_keys),
				KeySignature.KeyName.values(),
				new SpinnerItemSelectedListener<KeySignature.KeyName>() {

					public void onItemSelected(Spinner item,
							KeySignature.KeyName value) {
						keyNameSelected = value;
					};
				});

		spinnerKey.setSelection(defaultKeyName.ordinal());

		// setup time signature spinner and set to values from previous measure
		Spinner spinnerNumerator = (Spinner) findViewById(R.id.spinnerTimeNumerator);
		Spinner spinnerDenominator = (Spinner) findViewById(R.id.spinnerTimeDenominator);

		ArrayAdapter<Integer> timeAdapter = new ArrayAdapter<Integer>(context,
				android.R.layout.simple_list_item_1, timesValues);

		spinnerNumerator.setAdapter(timeAdapter);
		spinnerDenominator.setAdapter(timeAdapter);

		spinnerDenominator.setSelection(Arrays.binarySearch(timesValues,
				defaultDenominator));
		spinnerNumerator.setSelection(Arrays.binarySearch(timesValues,
				defaultNumerator));

		// setup OK button
		((Button) findViewById(R.id.button_ok))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Bundle measureParamters = new Bundle();
						// get measure count
						NumberPicker pickerCount = (NumberPicker) findViewById(R.id.pickerMeasureCount);
						measureParamters.putInt(KEY_MEASURE_COUNT,
								(Integer) pickerCount.getValue());

						// get measure new line
						CheckBox newlineCheck = (CheckBox) findViewById(R.id.checkBoxNewLine);
						measureParamters.putBoolean(KEY_MEASURE_NEWLINE,
								newlineCheck.isChecked());

						// get checkbox for page break
						CheckBox pageBreakCheck = (CheckBox) findViewById(R.id.checkBoxPageBreak);
						measureParamters.putBoolean(KEY_MEASURE_PAGEBREAK,
								pageBreakCheck.isChecked());

						// get measure repeat
						repeatStart = ((CheckBox) findViewById(R.id.checkBoxRepeatStart))
								.isChecked();
						repeatStop = ((CheckBox) findViewById(R.id.checkBoxRepeatStop))
								.isChecked();
						repeatEnding = ((CheckBox) findViewById(R.id.checkBoxRepeatEnding))
								.isChecked();

						measureParamters.putBoolean(KEY_MEASURE_REPEAT_START,
								repeatStart);
						measureParamters.putBoolean(KEY_MEASURE_REPEAT_STOP,
								repeatStop);
						measureParamters.putBoolean(KEY_MEASURE_REPEAT_ENDING,
								repeatEnding);

						// get measure key
						KeySignature key = new KeySignature(keyNameSelected);
						measureParamters.putParcelable(
								KEY_MEASURE_KEYSIGNATURE, key);
						if (keyNameSelected != defaultKeyName)
							measureParamters.putBoolean(KEY_MEASURE_KEYCHANGE,
									true);
						else
							measureParamters.putBoolean(KEY_MEASURE_KEYCHANGE,
									false);

						// get measure time
						int numerator = (Integer) ((Spinner) findViewById(R.id.spinnerTimeNumerator))
								.getSelectedItem();
						int denominator = (Integer) ((Spinner) findViewById(R.id.spinnerTimeDenominator))
								.getSelectedItem();
						TimeSignature time = new TimeSignature(numerator,
								denominator);
						measureParamters.putParcelable(
								KEY_MEASURE_TIMESIGNATURE, time);

						if (numerator != defaultNumerator
								|| denominator != defaultDenominator)
							measureParamters.putBoolean(KEY_MEASURE_TIMECHANGE,
									true);
						else
							measureParamters.putBoolean(KEY_MEASURE_TIMECHANGE,
									false);
						if (setupListener != null)
							setupListener.onMeasureSetup(measureParamters);

						dismiss();
					}
				});

		((Button) findViewById(R.id.button_cancel))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (setupListener != null)
							setupListener.onMeasureSetup(null);

						cancel();
					}
				});

		// TODO: Enable change of time and key one add
		switch (mode)
			{
			case EDIT:
				// disable number spinner if in edit mode
				pickerCount.setEnabled(false);
				break;

			case ADD:
				// disable key and time spinners in add mode
				spinnerDenominator.setEnabled(false);
				spinnerNumerator.setEnabled(false);
				spinnerKey.setEnabled(false);
				break;

			default:
				break;
			}

	}

}

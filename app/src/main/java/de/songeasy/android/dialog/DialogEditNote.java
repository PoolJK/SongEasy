/*
 * Copyright (C) 2014 Joerg Krein
 */
package de.songeasy.android.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import de.songeasy.android.R;
import de.songeasy.android.interfaces.OnNoteEditListener;
import de.songeasy.android.score.Beam;
import de.songeasy.android.score.MusicFont;
import de.songeasy.android.score.Note;
import de.songeasy.android.score.Note.NoteAccidental;
import de.songeasy.android.score.Note.TieType;
import de.songeasy.android.score.TimedElement;
import de.songeasy.android.score.TimedElement.NoteLength;

/**
 interface OnNoteEditListener {
 abstract void onNoteEdit(Note note);
 }
 **/

/**
 * Dialog class for editing properties of a note. Lets user change length value
 * and other attributes.
 * 
 * @author krein
 */
public class DialogEditNote extends Dialog {

	// string with class name for logging
	private final String TAG = this.getClass().getSimpleName();

	private Context context;
	private Note note;
	private OnNoteEditListener onNoteEditListener;
	/**
	 * String array with note accidentals to be shown in spinner.
	 */
	private String[] accidentalSigns = {
			new String(),
			new String(MusicFont.AccidentalSymbols.BSIGN.getCode())
					+ new String(MusicFont.SpecialSymbols.SPACE.getCode())
					+ new String(MusicFont.AccidentalSymbols.BSIGN.getCode()),
			new String(MusicFont.AccidentalSymbols.BSIGN.getCode()),
			new String(MusicFont.AccidentalSymbols.NATURAL.getCode()),
			new String(MusicFont.AccidentalSymbols.CROSS.getCode()),
			new String(MusicFont.AccidentalSymbols.CROSS.getCode())
					+ new String(MusicFont.SpecialSymbols.SPACE.getCode())
					+ new String(MusicFont.AccidentalSymbols.CROSS.getCode()) };

	/**
	 * ArrayAdapter for showing special accidental characters with MusicFont
	 * typeface.
	 * 
	 * @author krein
	 */
	static class AccidentalAdapter extends ArrayAdapter<String> {

		/** default text size for accidentals */
		public final static float ACCIDENTALREFERENCETEXTSIZE = 45.0f;


		public AccidentalAdapter(Context _context, int _resource,
				String[] strings) {
			super(_context, _resource, strings);
		}


		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View v = super.getView(position, convertView, parent);
			((TextView) v).setTypeface(MusicFont.getMusicTypeface());
			// ((TextView) v).setTextSize(ACCIDENTALREFERENCETEXTSIZE);
			return v;
		}


		@Override
		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {

			View v = super.getDropDownView(position, convertView, parent);
			((TextView) v).setTypeface(MusicFont.getMusicTypeface());
			return v;
		}

	}


	public void setOnNoteEditListener(OnNoteEditListener listener) {
		onNoteEditListener = listener;
	}


	/**
	 * Constructor of dialog.
	 * 
	 * @param context
	 *            Context needed for accessing app resources
	 * @param note
	 *            Note to be edit/create
	 */
	public DialogEditNote(Context context, Note note) {
		super(context);
		this.context = context;
		this.note = note;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {

		setContentView(R.layout.lay_dlg_edit_notes);

		setTitle(R.string.title_edit_notes);

		// register OnClickListener for ok button
		((Button) findViewById(R.id.button_ok))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						// evaluate checkbox dotted
						if (((CheckBox) findViewById(R.id.checkBoxDotted))
								.isChecked()) {
							note.isDotted = true;
						} else {
							note.isDotted = false;
						}

						// evaluate checkbox tie start
						if (((CheckBox) findViewById(R.id.checkBoxTie))
								.isChecked()) {
							note.getTies().add(TieType.START);
						} else {
							note.getTies().clear();
						}

						// evaluate check boxes beam
						// only note values shorter than quarter can be beamed
						if (note.isShorter(TimedElement.NoteLength.QUARTER)) {

							if (((CheckBox) findViewById(R.id.checkBoxBeamBegin))
									.isChecked()) {
								note.getBeams().clear();
								note.addBeam(new Beam(Beam.Type.BEGIN, 0));
							} else {
								if (((CheckBox) findViewById(R.id.checkBoxBeamContinue))
										.isChecked()) {
									note.getBeams().clear();
									note.addBeam(new Beam(Beam.Type.CONTINUE, 0));
								} else {
									if (((CheckBox) findViewById(R.id.checkBoxBeamEnd))
											.isChecked()) {
										note.getBeams().clear();
										note.addBeam(new Beam(Beam.Type.END, 0));
									} else {
										note.getBeams().clear();
									}
								}
							}
						} else {
							// show error if user tries to activate beams for
							// notes longer than eighth
							if (((CheckBox) findViewById(R.id.checkBoxBeamBegin))
									.isChecked()
									|| ((CheckBox) findViewById(R.id.checkBoxBeamContinue))
											.isChecked()
									|| ((CheckBox) findViewById(R.id.checkBoxBeamEnd))
											.isChecked())
								Toast.makeText(
										context,
										context.getResources().getString(
												R.string.err_note_too_long),
										Toast.LENGTH_SHORT).show();

						}

						// evaluate spinner accidental
						// TODO: hard coded relationship between enum and
						// spinner postition :-(
						switch (((Spinner) findViewById(R.id.spinnerAccidentals))
								.getSelectedItemPosition())
							{
							case 0:
								note.accidental = NoteAccidental.NONE;
								break;

							case 1:
								note.accidental = NoteAccidental.DOUBLE_FLAT;
								break;

							case 2:
								note.accidental = NoteAccidental.FLAT;
								break;

							case 3:
								note.accidental = NoteAccidental.NATURAL;
								break;

							case 4:
								note.accidental = NoteAccidental.SHARP;
								break;

							case 5:
								note.accidental = NoteAccidental.DOUBLE_SHARP;
								break;

							default:
								note.accidental = NoteAccidental.NONE;
								break;
							}

						if (onNoteEditListener != null) {
							onNoteEditListener.onNoteEdit(note);
						}
						dismiss();
					}
				});

		// register OnClickListener for cancel button
		((Button) findViewById(R.id.button_cancel))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (onNoteEditListener != null)
							onNoteEditListener.onNoteEdit(null);
						cancel();
					}
				});

		// register OnClickListener for shorter button
		((ImageButton) findViewById(R.id.imageButtonShorter))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						switch (note.getLength())
							{

							case FULL:
								note.setLength(NoteLength.HALF);
								setLengthImage();
								break;

							case HALF:
								note.setLength(NoteLength.QUARTER);
								setLengthImage();
								break;

							case QUARTER:
								note.setLength(NoteLength.EIGHTH);
								setLengthImage();
								break;

							case EIGHTH:
								note.setLength(NoteLength.SIXTEENTH);
								setLengthImage();
								break;

							case SIXTEENTH:
								note.setLength(NoteLength.THIRTYSECOND);
								setLengthImage();
								break;

							default:
								break;
							}

					}
				});

		// register OnClickListener for longer button
		((ImageButton) findViewById(R.id.imageButtonLonger))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						switch (note.getLength())
							{

							case THIRTYSECOND:
								note.setLength(NoteLength.SIXTEENTH);
								setLengthImage();
								break;

							case SIXTEENTH:
								note.setLength(NoteLength.EIGHTH);
								setLengthImage();
								break;

							case EIGHTH:
								note.setLength(NoteLength.QUARTER);
								setLengthImage();
								break;

							case QUARTER:
								note.setLength(NoteLength.HALF);
								setLengthImage();
								break;

							case HALF:
								note.setLength(NoteLength.FULL);
								setLengthImage();
								break;

							default:
								break;
							}
					}
				});

		// register OnClickListener for checkbox beam begin
		((CheckBox) findViewById(R.id.checkBoxBeamBegin))
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// uncheck beam continue and end
						if (isChecked) {
							((CheckBox) findViewById(R.id.checkBoxBeamContinue))
									.setChecked(false);
							((CheckBox) findViewById(R.id.checkBoxBeamEnd))
									.setChecked(false);
						}

					}
				});

		// register OnClickListener for checkbox beam continue
		((CheckBox) findViewById(R.id.checkBoxBeamContinue))
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// uncheck beam begin and end
						if (isChecked) {
							((CheckBox) findViewById(R.id.checkBoxBeamBegin))
									.setChecked(false);
							((CheckBox) findViewById(R.id.checkBoxBeamEnd))
									.setChecked(false);
						}

					}
				});

		// register OnClickListener for checkbox beam end
		((CheckBox) findViewById(R.id.checkBoxBeamEnd))
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// uncheck beam continue and begin
						if (isChecked) {
							((CheckBox) findViewById(R.id.checkBoxBeamBegin))
									.setChecked(false);
							((CheckBox) findViewById(R.id.checkBoxBeamContinue))
									.setChecked(false);
						}

					}
				});

		// set image with note length according to current note value
		setLengthImage();

		// set dotted radio button according to current note value
		if (note.isDotted)
			((CheckBox) findViewById(R.id.checkBoxDotted)).setChecked(true);

		// set array adapter for accidental spinner
		AccidentalAdapter accidentalAdapter = new AccidentalAdapter(context,
				R.layout.spinner_accidental_list_item, accidentalSigns);

		((Spinner) findViewById(R.id.spinnerAccidentals))
				.setAdapter(accidentalAdapter);

		// preselect accidentals according to current note value
		((Spinner) findViewById(R.id.spinnerAccidentals))
				.setSelection(note.accidental.ordinal());

		// preselect radiogroup beam
		if (note.getBeams().isEmpty() == false) {
			if (note.getBeams().get(0).getType() == Beam.Type.BEGIN)
				((CheckBox) findViewById(R.id.checkBoxBeamBegin))
						.setChecked(true);

			if (note.getBeams().get(0).getType() == Beam.Type.CONTINUE)
				((CheckBox) findViewById(R.id.checkBoxBeamContinue))
						.setChecked(true);

			if (note.getBeams().get(0).getType() == Beam.Type.END)
				((CheckBox) findViewById(R.id.checkBoxBeamEnd))
						.setChecked(true);
		}
		// preselect checkbox tie
		if (note.getTies().contains(TieType.START))
			((CheckBox) findViewById(R.id.checkBoxTie)).setChecked(true);

		super.onCreate(savedInstanceState);
	}


	/**
	 * Set image of ImageView according to current note length.
	 */
	private void setLengthImage() {

		switch (note.getLength())
			{

			case FULL:
				((ImageView) findViewById(R.id.imageViewNoteLength))
						.setImageResource(R.drawable.ic_note_full);
				break;

			case HALF:
				((ImageView) findViewById(R.id.imageViewNoteLength))
						.setImageResource(R.drawable.ic_note_half);
				break;

			case QUARTER:
				((ImageView) findViewById(R.id.imageViewNoteLength))
						.setImageResource(R.drawable.ic_note_4th);
				break;

			case EIGHTH:
				((ImageView) findViewById(R.id.imageViewNoteLength))
						.setImageResource(R.drawable.ic_note_8th);
				break;

			case SIXTEENTH:
				((ImageView) findViewById(R.id.imageViewNoteLength))
						.setImageResource(R.drawable.ic_note_16th);
				break;

			case THIRTYSECOND:
				((ImageView) findViewById(R.id.imageViewNoteLength))
						.setImageResource(R.drawable.ic_note_32th);
				break;

			default:
				break;
			}
	}
}
